-- Stage 9: harden progress visibility for athlete, trainer and admin roles.
BEGIN;

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

DROP POLICY IF EXISTS "Users can view own results" ON public.results;
DROP POLICY IF EXISTS "Trainers can view managed group results" ON public.results;
DROP POLICY IF EXISTS "Admins can view all results" ON public.results;
DROP POLICY IF EXISTS "Users can create own results" ON public.results;
DROP POLICY IF EXISTS "Users can update own results" ON public.results;

CREATE POLICY "Users can view own results" ON public.results
    FOR SELECT TO authenticated
    USING (user_id = (SELECT auth.uid()));

CREATE POLICY "Trainers can view managed group results" ON public.results
    FOR SELECT TO authenticated
    USING (public.trainer_manages_user(user_id));

CREATE POLICY "Admins can view all results" ON public.results
    FOR SELECT TO authenticated
    USING (public.is_admin());

CREATE POLICY "Users can create own results" ON public.results
    FOR INSERT TO authenticated
    WITH CHECK (user_id = (SELECT auth.uid()));

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
    LEFT JOIN public.exercises AS e ON e.id = pr.exercise_id
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

GRANT SELECT ON public.personal_record_bests TO authenticated;

COMMENT ON VIEW public.personal_record_bests IS
    'Best personal record per user and exercise. Uses invoker security so personal_records RLS still controls athlete, trainer and admin visibility.';

COMMIT;
