-- Stage 7: allow administrators and assigned trainers to manage inactive groups.
BEGIN;

DROP POLICY IF EXISTS "Users can view active groups" ON public.groups;

CREATE POLICY "Users can view active groups" ON public.groups
    FOR SELECT TO authenticated
    USING (
        is_active
        OR public.trainer_manages_group(id)
    );

COMMIT;
