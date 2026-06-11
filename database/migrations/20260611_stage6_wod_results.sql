-- Stage 6: atomic WOD composition, result submission, PR calculation and leaderboard.
BEGIN;

CREATE OR REPLACE FUNCTION public.create_wod_with_exercises(
    p_name TEXT,
    p_format TEXT,
    p_target_group_id UUID,
    p_scheduled_date DATE,
    p_time_cap_seconds INTEGER,
    p_notes TEXT,
    p_exercises JSONB
)
RETURNS public.wods
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
DECLARE
    v_user_id UUID := (SELECT auth.uid());
    v_wod public.wods%ROWTYPE;
    v_item JSONB;
BEGIN
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Authentication required' USING ERRCODE = '28000';
    END IF;

    IF NOT public.is_trainer_or_admin() THEN
        RAISE EXCEPTION 'Trainer or admin role required' USING ERRCODE = '42501';
    END IF;

    IF p_target_group_id IS NULL
       OR NOT public.trainer_manages_group(p_target_group_id) THEN
        RAISE EXCEPTION 'Target group is not managed by current user'
            USING ERRCODE = '42501';
    END IF;

    IF p_exercises IS NULL
       OR jsonb_typeof(p_exercises) <> 'array'
       OR jsonb_array_length(p_exercises) = 0 THEN
        RAISE EXCEPTION 'At least one exercise is required' USING ERRCODE = '22023';
    END IF;

    INSERT INTO public.wods (
        name,
        format,
        target_group_id,
        trainer_id,
        scheduled_date,
        time_cap_seconds,
        notes
    )
    VALUES (
        p_name,
        p_format,
        p_target_group_id,
        v_user_id,
        p_scheduled_date,
        GREATEST(COALESCE(p_time_cap_seconds, 0), 0),
        p_notes
    )
    RETURNING * INTO v_wod;

    FOR v_item IN SELECT value FROM jsonb_array_elements(p_exercises)
    LOOP
        INSERT INTO public.wod_exercises (
            wod_id,
            exercise_id,
            rounds,
            recommended_weight_kg,
            custom_instruction
        )
        VALUES (
            v_wod.id,
            (v_item ->> 'exercise_id')::UUID,
            GREATEST(COALESCE((v_item ->> 'rounds')::INTEGER, 1), 1),
            GREATEST(COALESCE((v_item ->> 'recommended_weight_kg')::INTEGER, 0), 0),
            NULLIF(v_item ->> 'custom_instruction', '')
        );
    END LOOP;

    RETURN v_wod;
END;
$$;

CREATE OR REPLACE FUNCTION public.submit_wod_result(
    p_wod_id UUID,
    p_score NUMERIC,
    p_formatted_score TEXT
)
RETURNS public.results
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
DECLARE
    v_user_id UUID := (SELECT auth.uid());
    v_wod public.wods%ROWTYPE;
    v_result public.results%ROWTYPE;
    v_best NUMERIC;
    v_is_pr BOOLEAN;
BEGIN
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Authentication required' USING ERRCODE = '28000';
    END IF;

    IF p_score IS NULL OR p_score < 0 THEN
        RAISE EXCEPTION 'Score must be non-negative' USING ERRCODE = '22023';
    END IF;

    SELECT *
    INTO v_wod
    FROM public.wods
    WHERE id = p_wod_id
      AND (
          target_group_id IS NULL
          OR target_group_id = public.current_user_group_id()
          OR trainer_id = v_user_id
          OR public.is_admin()
      );

    IF NOT FOUND THEN
        RAISE EXCEPTION 'WOD is not available to current user' USING ERRCODE = '42501';
    END IF;

    IF v_wod.format = 'for_time' THEN
        SELECT MIN(score)
        INTO v_best
        FROM public.results
        WHERE wod_id = p_wod_id
          AND user_id = v_user_id;
        v_is_pr := v_best IS NULL OR p_score < v_best;
    ELSE
        SELECT MAX(score)
        INTO v_best
        FROM public.results
        WHERE wod_id = p_wod_id
          AND user_id = v_user_id;
        v_is_pr := v_best IS NULL OR p_score > v_best;
    END IF;

    INSERT INTO public.results (
        wod_id,
        user_id,
        score,
        formatted_score,
        is_pr,
        synced_at
    )
    VALUES (
        p_wod_id,
        v_user_id,
        p_score,
        NULLIF(p_formatted_score, ''),
        v_is_pr,
        NOW()
    )
    RETURNING * INTO v_result;

    RETURN v_result;
END;
$$;

CREATE OR REPLACE FUNCTION public.get_wod_leaderboard(p_wod_id UUID)
RETURNS TABLE (
    rank BIGINT,
    user_id UUID,
    full_name TEXT,
    score NUMERIC,
    formatted_score TEXT,
    is_pr BOOLEAN,
    completed_at TIMESTAMPTZ
)
LANGUAGE plpgsql
STABLE
SECURITY DEFINER
SET search_path = ''
AS $$
DECLARE
    v_wod public.wods%ROWTYPE;
BEGIN
    SELECT *
    INTO v_wod
    FROM public.wods
    WHERE id = p_wod_id
      AND (
          target_group_id IS NULL
          OR target_group_id = public.current_user_group_id()
          OR trainer_id = (SELECT auth.uid())
          OR public.is_admin()
      );

    IF NOT FOUND THEN
        RAISE EXCEPTION 'WOD is not available to current user' USING ERRCODE = '42501';
    END IF;

    RETURN QUERY
    WITH ranked_results AS (
        SELECT
            r.*,
            ROW_NUMBER() OVER (
                PARTITION BY r.user_id
                ORDER BY
                    CASE WHEN v_wod.format = 'for_time' THEN r.score END ASC,
                    CASE WHEN v_wod.format <> 'for_time' THEN r.score END DESC,
                    r.completed_at ASC
            ) AS user_position
        FROM public.results AS r
        WHERE r.wod_id = p_wod_id
    ),
    best_results AS (
        SELECT *
        FROM ranked_results
        WHERE user_position = 1
    )
    SELECT
        ROW_NUMBER() OVER (
            ORDER BY
                CASE WHEN v_wod.format = 'for_time' THEN br.score END ASC,
                CASE WHEN v_wod.format <> 'for_time' THEN br.score END DESC,
                br.completed_at ASC
        ) AS rank,
        br.user_id,
        p.full_name,
        br.score,
        br.formatted_score,
        br.is_pr,
        br.completed_at
    FROM best_results AS br
    JOIN public.profiles AS p ON p.id = br.user_id
    ORDER BY 1;
END;
$$;

REVOKE ALL ON FUNCTION public.create_wod_with_exercises(
    TEXT, TEXT, UUID, DATE, INTEGER, TEXT, JSONB
) FROM PUBLIC;
REVOKE ALL ON FUNCTION public.submit_wod_result(UUID, NUMERIC, TEXT) FROM PUBLIC;
REVOKE ALL ON FUNCTION public.get_wod_leaderboard(UUID) FROM PUBLIC;

GRANT EXECUTE ON FUNCTION public.create_wod_with_exercises(
    TEXT, TEXT, UUID, DATE, INTEGER, TEXT, JSONB
) TO authenticated;
GRANT EXECUTE ON FUNCTION public.submit_wod_result(UUID, NUMERIC, TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION public.get_wod_leaderboard(UUID) TO authenticated;

COMMIT;
