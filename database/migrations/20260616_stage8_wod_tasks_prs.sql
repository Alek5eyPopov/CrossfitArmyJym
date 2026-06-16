-- Stage 8: WOD task model, load types, RX/Optional variants and exercise PR history.
BEGIN;

ALTER TABLE public.exercises
ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true,
ADD COLUMN IF NOT EXISTS pr_unit TEXT,
ADD COLUMN IF NOT EXISTS pr_better_direction TEXT DEFAULT 'max',
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW();

CREATE TABLE IF NOT EXISTS public.load_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code TEXT UNIQUE NOT NULL CHECK (code ~ '^[a-z0-9_]+$'),
    name TEXT NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.training_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    rx_exercise_id UUID NOT NULL REFERENCES public.exercises(id) ON DELETE RESTRICT,
    load_type_id UUID NOT NULL REFERENCES public.load_types(id) ON DELETE RESTRICT,
    rx_load_description TEXT NOT NULL,
    optional_exercise_id UUID REFERENCES public.exercises(id) ON DELETE RESTRICT,
    optional_load_type_id UUID REFERENCES public.load_types(id) ON DELETE RESTRICT,
    optional_load_description TEXT,
    notes TEXT,
    is_active BOOLEAN DEFAULT true,
    created_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.wod_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wod_id UUID NOT NULL REFERENCES public.wods(id) ON DELETE CASCADE,
    source_task_id UUID REFERENCES public.training_tasks(id) ON DELETE SET NULL,
    position INTEGER NOT NULL DEFAULT 1 CHECK (position > 0),
    title TEXT NOT NULL,
    rx_exercise_id UUID NOT NULL REFERENCES public.exercises(id) ON DELETE RESTRICT,
    load_type_id UUID NOT NULL REFERENCES public.load_types(id) ON DELETE RESTRICT,
    rx_load_description TEXT NOT NULL,
    optional_exercise_id UUID REFERENCES public.exercises(id) ON DELETE RESTRICT,
    optional_load_type_id UUID REFERENCES public.load_types(id) ON DELETE RESTRICT,
    optional_load_description TEXT,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(wod_id, position)
);

CREATE TABLE IF NOT EXISTS public.personal_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    exercise_id UUID NOT NULL REFERENCES public.exercises(id) ON DELETE CASCADE,
    result_value NUMERIC,
    result_text TEXT NOT NULL,
    unit TEXT,
    achieved_at DATE NOT NULL DEFAULT CURRENT_DATE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_exercises_is_active ON public.exercises(is_active);
CREATE INDEX IF NOT EXISTS idx_load_types_code ON public.load_types(code);
CREATE INDEX IF NOT EXISTS idx_load_types_active ON public.load_types(is_active);
CREATE INDEX IF NOT EXISTS idx_training_tasks_exercise ON public.training_tasks(rx_exercise_id);
CREATE INDEX IF NOT EXISTS idx_training_tasks_load_type ON public.training_tasks(load_type_id);
CREATE INDEX IF NOT EXISTS idx_training_tasks_active ON public.training_tasks(is_active);
CREATE INDEX IF NOT EXISTS idx_wod_tasks_wod_position ON public.wod_tasks(wod_id, position);
CREATE INDEX IF NOT EXISTS idx_wod_tasks_rx_exercise ON public.wod_tasks(rx_exercise_id);
CREATE INDEX IF NOT EXISTS idx_personal_records_user ON public.personal_records(user_id);
CREATE INDEX IF NOT EXISTS idx_personal_records_exercise ON public.personal_records(exercise_id);
CREATE INDEX IF NOT EXISTS idx_personal_records_achieved ON public.personal_records(achieved_at DESC);

ALTER TABLE public.load_types ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.training_tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.wod_tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.personal_records ENABLE ROW LEVEL SECURITY;

INSERT INTO public.load_types (code, name, description)
VALUES
    ('amrap', 'AMRAP', 'As many rounds or reps as possible within a time window.'),
    ('emom', 'EMOM', 'Every minute or every N minutes on the minute.'),
    ('for_time', 'For Time', 'Complete the prescribed work as fast as possible.'),
    ('complex', 'Complex', 'A multi-part assignment with several movements or blocks.'),
    ('strength', 'Strength', 'Strength work such as sets, reps and load.'),
    ('tabata', 'Tabata', 'Interval protocol with work and rest windows.'),
    ('other', 'Other', 'Custom load type described by coach text.')
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_active = true,
    updated_at = NOW();

DROP POLICY IF EXISTS "Users can view active load types" ON public.load_types;
DROP POLICY IF EXISTS "Trainers and admins can create load types" ON public.load_types;
DROP POLICY IF EXISTS "Trainers and admins can update load types" ON public.load_types;
DROP POLICY IF EXISTS "Admins can delete load types" ON public.load_types;

CREATE POLICY "Users can view active load types" ON public.load_types
    FOR SELECT TO authenticated
    USING (is_active OR public.is_trainer_or_admin());

CREATE POLICY "Trainers and admins can create load types" ON public.load_types
    FOR INSERT TO authenticated
    WITH CHECK (public.is_trainer_or_admin());

CREATE POLICY "Trainers and admins can update load types" ON public.load_types
    FOR UPDATE TO authenticated
    USING (public.is_trainer_or_admin())
    WITH CHECK (public.is_trainer_or_admin());

CREATE POLICY "Admins can delete load types" ON public.load_types
    FOR DELETE TO authenticated
    USING (public.is_admin());

DROP POLICY IF EXISTS "Users can view exercises" ON public.exercises;
DROP POLICY IF EXISTS "Trainers and admins can create exercises" ON public.exercises;
DROP POLICY IF EXISTS "Authors and admins can update exercises" ON public.exercises;
DROP POLICY IF EXISTS "Trainers and admins can update exercises" ON public.exercises;
DROP POLICY IF EXISTS "Admins can delete exercises" ON public.exercises;

CREATE POLICY "Users can view exercises" ON public.exercises
    FOR SELECT TO authenticated
    USING (is_active OR public.is_trainer_or_admin());

CREATE POLICY "Trainers and admins can create exercises" ON public.exercises
    FOR INSERT TO authenticated
    WITH CHECK (public.is_trainer_or_admin());

CREATE POLICY "Trainers and admins can update exercises" ON public.exercises
    FOR UPDATE TO authenticated
    USING (public.is_trainer_or_admin())
    WITH CHECK (public.is_trainer_or_admin());

CREATE POLICY "Admins can delete exercises" ON public.exercises
    FOR DELETE TO authenticated
    USING (public.is_admin());

DROP POLICY IF EXISTS "Users can view active training tasks" ON public.training_tasks;
DROP POLICY IF EXISTS "Trainers and admins can create training tasks" ON public.training_tasks;
DROP POLICY IF EXISTS "Trainers and admins can update training tasks" ON public.training_tasks;
DROP POLICY IF EXISTS "Admins can delete training tasks" ON public.training_tasks;

CREATE POLICY "Users can view active training tasks" ON public.training_tasks
    FOR SELECT TO authenticated
    USING (is_active OR public.is_trainer_or_admin());

CREATE POLICY "Trainers and admins can create training tasks" ON public.training_tasks
    FOR INSERT TO authenticated
    WITH CHECK (public.is_trainer_or_admin());

CREATE POLICY "Trainers and admins can update training tasks" ON public.training_tasks
    FOR UPDATE TO authenticated
    USING (public.is_trainer_or_admin())
    WITH CHECK (public.is_trainer_or_admin());

CREATE POLICY "Admins can delete training tasks" ON public.training_tasks
    FOR DELETE TO authenticated
    USING (public.is_admin());

DROP POLICY IF EXISTS "Users can view available wod tasks" ON public.wod_tasks;
DROP POLICY IF EXISTS "Wod owners can create wod tasks" ON public.wod_tasks;
DROP POLICY IF EXISTS "Wod owners can update wod tasks" ON public.wod_tasks;
DROP POLICY IF EXISTS "Wod owners can delete wod tasks" ON public.wod_tasks;

CREATE POLICY "Users can view available wod tasks" ON public.wod_tasks
    FOR SELECT TO authenticated
    USING (
        EXISTS (
            SELECT 1
            FROM public.wods AS w
            WHERE w.id = wod_id
              AND (
                  w.target_group_id IS NULL
                  OR w.target_group_id = public.current_user_group_id()
                  OR w.trainer_id = (SELECT auth.uid())
                  OR public.is_admin()
              )
        )
    );

CREATE POLICY "Wod owners can create wod tasks" ON public.wod_tasks
    FOR INSERT TO authenticated
    WITH CHECK (
        EXISTS (
            SELECT 1
            FROM public.wods AS w
            WHERE w.id = wod_id
              AND (
                  public.is_admin()
                  OR w.trainer_id = (SELECT auth.uid())
              )
        )
    );

CREATE POLICY "Wod owners can update wod tasks" ON public.wod_tasks
    FOR UPDATE TO authenticated
    USING (
        EXISTS (
            SELECT 1
            FROM public.wods AS w
            WHERE w.id = wod_id
              AND (
                  public.is_admin()
                  OR w.trainer_id = (SELECT auth.uid())
              )
        )
    )
    WITH CHECK (
        EXISTS (
            SELECT 1
            FROM public.wods AS w
            WHERE w.id = wod_id
              AND (
                  public.is_admin()
                  OR w.trainer_id = (SELECT auth.uid())
              )
        )
    );

CREATE POLICY "Wod owners can delete wod tasks" ON public.wod_tasks
    FOR DELETE TO authenticated
    USING (
        EXISTS (
            SELECT 1
            FROM public.wods AS w
            WHERE w.id = wod_id
              AND (
                  public.is_admin()
                  OR w.trainer_id = (SELECT auth.uid())
              )
        )
    );

DROP POLICY IF EXISTS "Users can view own personal records" ON public.personal_records;
DROP POLICY IF EXISTS "Trainers can view managed personal records" ON public.personal_records;
DROP POLICY IF EXISTS "Admins can view all personal records" ON public.personal_records;
DROP POLICY IF EXISTS "Users can create own personal records" ON public.personal_records;
DROP POLICY IF EXISTS "Admins can manage personal records" ON public.personal_records;

CREATE POLICY "Users can view own personal records" ON public.personal_records
    FOR SELECT TO authenticated
    USING (user_id = (SELECT auth.uid()));

CREATE POLICY "Trainers can view managed personal records" ON public.personal_records
    FOR SELECT TO authenticated
    USING (public.trainer_manages_user(user_id));

CREATE POLICY "Admins can view all personal records" ON public.personal_records
    FOR SELECT TO authenticated
    USING (public.is_admin());

CREATE POLICY "Users can create own personal records" ON public.personal_records
    FOR INSERT TO authenticated
    WITH CHECK (user_id = (SELECT auth.uid()));

CREATE POLICY "Admins can manage personal records" ON public.personal_records
    FOR ALL TO authenticated
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

DROP POLICY IF EXISTS "Users can update own results" ON public.results;

CREATE OR REPLACE FUNCTION public.resolve_load_type_id(
    p_load_type_id UUID,
    p_load_type_code TEXT
)
RETURNS UUID
LANGUAGE plpgsql
STABLE
SECURITY DEFINER
SET search_path = ''
AS $$
DECLARE
    v_load_type_id UUID;
BEGIN
    IF p_load_type_id IS NOT NULL THEN
        RETURN p_load_type_id;
    END IF;

    SELECT lt.id
    INTO v_load_type_id
    FROM public.load_types AS lt
    WHERE lt.code = LOWER(NULLIF(BTRIM(p_load_type_code), ''))
      AND lt.is_active
    LIMIT 1;

    RETURN v_load_type_id;
END;
$$;

CREATE OR REPLACE FUNCTION public.create_wod_with_tasks(
    p_name TEXT,
    p_format TEXT,
    p_target_group_id UUID,
    p_scheduled_date DATE,
    p_time_cap_seconds INTEGER,
    p_notes TEXT,
    p_tasks JSONB
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
    v_position INTEGER := 0;
    v_source_task public.training_tasks%ROWTYPE;
    v_source_task_id UUID;
    v_rx_exercise_id UUID;
    v_load_type_id UUID;
    v_optional_exercise_id UUID;
    v_optional_load_type_id UUID;
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

    IF p_tasks IS NULL
       OR jsonb_typeof(p_tasks) <> 'array'
       OR jsonb_array_length(p_tasks) = 0 THEN
        RAISE EXCEPTION 'At least one task is required' USING ERRCODE = '22023';
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
        NULLIF(BTRIM(p_name), ''),
        p_format,
        p_target_group_id,
        v_user_id,
        p_scheduled_date,
        GREATEST(COALESCE(p_time_cap_seconds, 0), 0),
        NULLIF(BTRIM(p_notes), '')
    )
    RETURNING * INTO v_wod;

    FOR v_item IN SELECT value FROM jsonb_array_elements(p_tasks)
    LOOP
        v_position := v_position + 1;
        v_source_task_id := NULLIF(v_item ->> 'source_task_id', '')::UUID;
        v_source_task := NULL;

        IF v_source_task_id IS NOT NULL THEN
            SELECT *
            INTO v_source_task
            FROM public.training_tasks
            WHERE id = v_source_task_id
              AND is_active;
        END IF;

        v_rx_exercise_id := COALESCE(
            NULLIF(v_item ->> 'rx_exercise_id', '')::UUID,
            NULLIF(v_item ->> 'exercise_id', '')::UUID,
            v_source_task.rx_exercise_id
        );
        v_load_type_id := COALESCE(
            public.resolve_load_type_id(
                NULLIF(v_item ->> 'load_type_id', '')::UUID,
                v_item ->> 'load_type_code'
            ),
            v_source_task.load_type_id
        );
        v_optional_exercise_id := COALESCE(
            NULLIF(v_item ->> 'optional_exercise_id', '')::UUID,
            v_source_task.optional_exercise_id
        );
        v_optional_load_type_id := COALESCE(
            public.resolve_load_type_id(
                NULLIF(v_item ->> 'optional_load_type_id', '')::UUID,
                v_item ->> 'optional_load_type_code'
            ),
            v_source_task.optional_load_type_id
        );

        IF v_rx_exercise_id IS NULL OR v_load_type_id IS NULL THEN
            RAISE EXCEPTION 'Task requires an exercise and load type'
                USING ERRCODE = '22023';
        END IF;

        INSERT INTO public.wod_tasks (
            wod_id,
            source_task_id,
            position,
            title,
            rx_exercise_id,
            load_type_id,
            rx_load_description,
            optional_exercise_id,
            optional_load_type_id,
            optional_load_description,
            notes
        )
        VALUES (
            v_wod.id,
            v_source_task_id,
            COALESCE(NULLIF(v_item ->> 'position', '')::INTEGER, v_position),
            COALESCE(
                NULLIF(BTRIM(v_item ->> 'title'), ''),
                v_source_task.title,
                'Task ' || v_position
            ),
            v_rx_exercise_id,
            v_load_type_id,
            COALESCE(
                NULLIF(BTRIM(v_item ->> 'rx_load_description'), ''),
                NULLIF(BTRIM(v_item ->> 'load_description'), ''),
                v_source_task.rx_load_description,
                'See coach notes'
            ),
            v_optional_exercise_id,
            v_optional_load_type_id,
            COALESCE(
                NULLIF(BTRIM(v_item ->> 'optional_load_description'), ''),
                v_source_task.optional_load_description
            ),
            COALESCE(
                NULLIF(BTRIM(v_item ->> 'notes'), ''),
                v_source_task.notes
            )
        );
    END LOOP;

    RETURN v_wod;
END;
$$;

CREATE OR REPLACE FUNCTION public.submit_personal_record(
    p_exercise_id UUID,
    p_result_value NUMERIC,
    p_result_text TEXT,
    p_unit TEXT,
    p_achieved_at DATE,
    p_notes TEXT
)
RETURNS public.personal_records
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
DECLARE
    v_user_id UUID := (SELECT auth.uid());
    v_record public.personal_records%ROWTYPE;
BEGIN
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Authentication required' USING ERRCODE = '28000';
    END IF;

    IF p_exercise_id IS NULL THEN
        RAISE EXCEPTION 'Exercise is required' USING ERRCODE = '22023';
    END IF;

    IF NULLIF(BTRIM(p_result_text), '') IS NULL THEN
        RAISE EXCEPTION 'Result text is required' USING ERRCODE = '22023';
    END IF;

    INSERT INTO public.personal_records (
        user_id,
        exercise_id,
        result_value,
        result_text,
        unit,
        achieved_at,
        notes
    )
    VALUES (
        v_user_id,
        p_exercise_id,
        p_result_value,
        NULLIF(BTRIM(p_result_text), ''),
        NULLIF(BTRIM(p_unit), ''),
        COALESCE(p_achieved_at, CURRENT_DATE),
        NULLIF(BTRIM(p_notes), '')
    )
    RETURNING * INTO v_record;

    RETURN v_record;
END;
$$;

CREATE OR REPLACE VIEW public.personal_record_bests
WITH (security_invoker = true) AS
WITH ranked_records AS (
    SELECT
        pr.*,
        e.name AS exercise_name,
        ROW_NUMBER() OVER (
            PARTITION BY pr.user_id, pr.exercise_id
            ORDER BY
                CASE WHEN COALESCE(e.pr_better_direction, 'max') = 'max' THEN pr.result_value END DESC NULLS LAST,
                CASE WHEN COALESCE(e.pr_better_direction, 'max') = 'min' THEN pr.result_value END ASC NULLS LAST,
                pr.achieved_at DESC,
                pr.created_at DESC
        ) AS record_rank
    FROM public.personal_records AS pr
    JOIN public.exercises AS e ON e.id = pr.exercise_id
)
SELECT
    id,
    user_id,
    exercise_id,
    exercise_name,
    result_value,
    result_text,
    unit,
    achieved_at,
    notes,
    created_at
FROM ranked_records
WHERE record_rank = 1;

REVOKE ALL ON FUNCTION public.resolve_load_type_id(UUID, TEXT) FROM PUBLIC;
REVOKE ALL ON FUNCTION public.create_wod_with_tasks(
    TEXT, TEXT, UUID, DATE, INTEGER, TEXT, JSONB
) FROM PUBLIC;
REVOKE ALL ON FUNCTION public.submit_personal_record(
    UUID, NUMERIC, TEXT, TEXT, DATE, TEXT
) FROM PUBLIC;

GRANT EXECUTE ON FUNCTION public.resolve_load_type_id(UUID, TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION public.create_wod_with_tasks(
    TEXT, TEXT, UUID, DATE, INTEGER, TEXT, JSONB
) TO authenticated;
GRANT EXECUTE ON FUNCTION public.submit_personal_record(
    UUID, NUMERIC, TEXT, TEXT, DATE, TEXT
) TO authenticated;

GRANT SELECT ON public.load_types TO authenticated;
GRANT INSERT, UPDATE, DELETE ON public.load_types TO authenticated;

GRANT SELECT ON public.training_tasks TO authenticated;
GRANT INSERT, UPDATE, DELETE ON public.training_tasks TO authenticated;

GRANT SELECT ON public.wod_tasks TO authenticated;
GRANT INSERT, UPDATE, DELETE ON public.wod_tasks TO authenticated;

GRANT SELECT, INSERT, UPDATE, DELETE ON public.personal_records TO authenticated;
GRANT SELECT ON public.personal_record_bests TO authenticated;

COMMIT;
