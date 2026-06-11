-- Stage 2: role security, non-recursive RLS helpers and atomic bookings.
-- Apply this migration in the Supabase SQL Editor after supabase_schema.sql.

BEGIN;

-- Security-definer helpers avoid recursive reads of profiles from profiles RLS.
CREATE OR REPLACE FUNCTION public.current_user_role()
RETURNS TEXT
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = ''
AS $$
    SELECT p.role
    FROM public.profiles AS p
    WHERE p.id = (SELECT auth.uid());
$$;

CREATE OR REPLACE FUNCTION public.current_user_group_id()
RETURNS UUID
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = ''
AS $$
    SELECT p.group_id
    FROM public.profiles AS p
    WHERE p.id = (SELECT auth.uid());
$$;

CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = ''
AS $$
    SELECT COALESCE(public.current_user_role() = 'admin', false);
$$;

CREATE OR REPLACE FUNCTION public.is_trainer_or_admin()
RETURNS BOOLEAN
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = ''
AS $$
    SELECT COALESCE(
        public.current_user_role() IN ('trainer', 'admin'),
        false
    );
$$;

CREATE OR REPLACE FUNCTION public.trainer_manages_group(p_group_id UUID)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = ''
AS $$
    SELECT public.is_admin() OR EXISTS (
        SELECT 1
        FROM public.groups AS g
        WHERE g.id = p_group_id
          AND g.trainer_id = (SELECT auth.uid())
    );
$$;

CREATE OR REPLACE FUNCTION public.trainer_owns_class(p_class_id UUID)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = ''
AS $$
    SELECT public.is_admin() OR EXISTS (
        SELECT 1
        FROM public.classes AS c
        WHERE c.id = p_class_id
          AND c.trainer_id = (SELECT auth.uid())
    );
$$;

CREATE OR REPLACE FUNCTION public.trainer_manages_user(p_user_id UUID)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = ''
AS $$
    SELECT public.is_admin() OR EXISTS (
        SELECT 1
        FROM public.profiles AS athlete
        JOIN public.groups AS g ON g.id = athlete.group_id
        WHERE athlete.id = p_user_id
          AND g.trainer_id = (SELECT auth.uid())
    );
$$;

REVOKE ALL ON FUNCTION public.current_user_role() FROM PUBLIC;
REVOKE ALL ON FUNCTION public.current_user_group_id() FROM PUBLIC;
REVOKE ALL ON FUNCTION public.is_admin() FROM PUBLIC;
REVOKE ALL ON FUNCTION public.is_trainer_or_admin() FROM PUBLIC;
REVOKE ALL ON FUNCTION public.trainer_manages_group(UUID) FROM PUBLIC;
REVOKE ALL ON FUNCTION public.trainer_owns_class(UUID) FROM PUBLIC;
REVOKE ALL ON FUNCTION public.trainer_manages_user(UUID) FROM PUBLIC;

GRANT EXECUTE ON FUNCTION public.current_user_role() TO authenticated;
GRANT EXECUTE ON FUNCTION public.current_user_group_id() TO authenticated;
GRANT EXECUTE ON FUNCTION public.is_admin() TO authenticated;
GRANT EXECUTE ON FUNCTION public.is_trainer_or_admin() TO authenticated;
GRANT EXECUTE ON FUNCTION public.trainer_manages_group(UUID) TO authenticated;
GRANT EXECUTE ON FUNCTION public.trainer_owns_class(UUID) TO authenticated;
GRANT EXECUTE ON FUNCTION public.trainer_manages_user(UUID) TO authenticated;

-- Users may edit presentation fields, but never their role, group or status.
CREATE OR REPLACE FUNCTION public.protect_profile_privileged_fields()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
BEGIN
    IF COALESCE((SELECT auth.role()), '') <> 'service_role'
       AND NOT public.is_admin()
       AND (
           NEW.id IS DISTINCT FROM OLD.id
           OR NEW.email IS DISTINCT FROM OLD.email
           OR NEW.role IS DISTINCT FROM OLD.role
           OR NEW.group_id IS DISTINCT FROM OLD.group_id
           OR NEW.is_active IS DISTINCT FROM OLD.is_active
           OR NEW.created_at IS DISTINCT FROM OLD.created_at
       )
    THEN
        RAISE EXCEPTION 'Only administrators can change protected profile fields'
            USING ERRCODE = '42501';
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS protect_profile_privileged_fields ON public.profiles;
CREATE TRIGGER protect_profile_privileged_fields
    BEFORE UPDATE ON public.profiles
    FOR EACH ROW
    EXECUTE FUNCTION public.protect_profile_privileged_fields();

-- New accounts always start as athletes. User metadata cannot grant privileges.
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
BEGIN
    INSERT INTO public.profiles (id, email, full_name, role, is_active)
    VALUES (
        NEW.id,
        NEW.email,
        COALESCE(
            NULLIF(BTRIM(NEW.raw_user_meta_data->>'full_name'), ''),
            split_part(NEW.email, '@', 1)
        ),
        'athlete',
        true
    );
    RETURN NEW;
END;
$$;

-- Replace policies so role checks do not recursively query profiles.
DROP POLICY IF EXISTS "Users can view own profile" ON public.profiles;
DROP POLICY IF EXISTS "Athletes can view group profiles" ON public.profiles;
DROP POLICY IF EXISTS "Trainers can view all profiles" ON public.profiles;
DROP POLICY IF EXISTS "Admins full access profiles" ON public.profiles;
DROP POLICY IF EXISTS "Users can update own profile" ON public.profiles;
DROP POLICY IF EXISTS "Users can view profiles in own group" ON public.profiles;
DROP POLICY IF EXISTS "Trainers and admins can view profiles" ON public.profiles;
DROP POLICY IF EXISTS "Admins can manage profiles" ON public.profiles;
DROP POLICY IF EXISTS "Users can update own public profile" ON public.profiles;

CREATE POLICY "Users can view own profile" ON public.profiles
    FOR SELECT TO authenticated
    USING (id = (SELECT auth.uid()));

CREATE POLICY "Users can view profiles in own group" ON public.profiles
    FOR SELECT TO authenticated
    USING (
        group_id IS NOT NULL
        AND group_id = public.current_user_group_id()
    );

CREATE POLICY "Trainers and admins can view profiles" ON public.profiles
    FOR SELECT TO authenticated
    USING (
        public.is_admin()
        OR public.trainer_manages_user(id)
    );

CREATE POLICY "Admins can manage profiles" ON public.profiles
    FOR ALL TO authenticated
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

CREATE POLICY "Users can update own public profile" ON public.profiles
    FOR UPDATE TO authenticated
    USING (id = (SELECT auth.uid()))
    WITH CHECK (id = (SELECT auth.uid()));

DROP POLICY IF EXISTS "Trainers can create groups" ON public.groups;
DROP POLICY IF EXISTS "Trainers can update own groups" ON public.groups;
DROP POLICY IF EXISTS "Admins can delete groups" ON public.groups;
DROP POLICY IF EXISTS "Users can view active groups" ON public.groups;
DROP POLICY IF EXISTS "Trainers and admins can create groups" ON public.groups;
DROP POLICY IF EXISTS "Trainers and admins can update groups" ON public.groups;

CREATE POLICY "Users can view active groups" ON public.groups
    FOR SELECT TO authenticated
    USING (is_active);

CREATE POLICY "Trainers and admins can create groups" ON public.groups
    FOR INSERT TO authenticated
    WITH CHECK (
        public.is_admin()
        OR (
            public.current_user_role() = 'trainer'
            AND trainer_id = (SELECT auth.uid())
        )
    );

CREATE POLICY "Trainers and admins can update groups" ON public.groups
    FOR UPDATE TO authenticated
    USING (public.trainer_manages_group(id))
    WITH CHECK (
        public.is_admin()
        OR trainer_id = (SELECT auth.uid())
    );

CREATE POLICY "Admins can delete groups" ON public.groups
    FOR DELETE TO authenticated
    USING (public.is_admin());

DROP POLICY IF EXISTS "Trainers and admins can create exercises" ON public.exercises;
DROP POLICY IF EXISTS "Authors can update own exercises" ON public.exercises;
DROP POLICY IF EXISTS "Admins can delete exercises" ON public.exercises;
DROP POLICY IF EXISTS "Users can view exercises" ON public.exercises;
DROP POLICY IF EXISTS "Authors and admins can update exercises" ON public.exercises;

CREATE POLICY "Users can view exercises" ON public.exercises
    FOR SELECT TO authenticated
    USING (true);

CREATE POLICY "Trainers and admins can create exercises" ON public.exercises
    FOR INSERT TO authenticated
    WITH CHECK (
        public.is_trainer_or_admin()
        AND (
            public.is_admin()
            OR created_by = (SELECT auth.uid())
        )
    );

CREATE POLICY "Authors and admins can update exercises" ON public.exercises
    FOR UPDATE TO authenticated
    USING (
        public.is_admin()
        OR created_by = (SELECT auth.uid())
    )
    WITH CHECK (
        public.is_admin()
        OR created_by = (SELECT auth.uid())
    );

CREATE POLICY "Admins can delete exercises" ON public.exercises
    FOR DELETE TO authenticated
    USING (public.is_admin());

DROP POLICY IF EXISTS "Athletes can view group wods" ON public.wods;
DROP POLICY IF EXISTS "Trainers can view all wods" ON public.wods;
DROP POLICY IF EXISTS "Trainers can create wods" ON public.wods;
DROP POLICY IF EXISTS "Trainers can update own wods" ON public.wods;
DROP POLICY IF EXISTS "Admins can delete wods" ON public.wods;
DROP POLICY IF EXISTS "Users can view assigned wods" ON public.wods;
DROP POLICY IF EXISTS "Trainers and admins can create wods" ON public.wods;
DROP POLICY IF EXISTS "Trainers and admins can update wods" ON public.wods;

CREATE POLICY "Users can view assigned wods" ON public.wods
    FOR SELECT TO authenticated
    USING (
        public.is_admin()
        OR trainer_id = (SELECT auth.uid())
        OR target_group_id IS NULL
        OR target_group_id = public.current_user_group_id()
    );

CREATE POLICY "Trainers and admins can create wods" ON public.wods
    FOR INSERT TO authenticated
    WITH CHECK (
        public.is_trainer_or_admin()
        AND (
            public.is_admin()
            OR (
                trainer_id = (SELECT auth.uid())
                AND target_group_id IS NOT NULL
                AND public.trainer_manages_group(target_group_id)
            )
        )
    );

CREATE POLICY "Trainers and admins can update wods" ON public.wods
    FOR UPDATE TO authenticated
    USING (
        public.is_admin()
        OR trainer_id = (SELECT auth.uid())
    )
    WITH CHECK (
        public.is_admin()
        OR trainer_id = (SELECT auth.uid())
    );

CREATE POLICY "Admins can delete wods" ON public.wods
    FOR DELETE TO authenticated
    USING (public.is_admin());

DROP POLICY IF EXISTS "Trainers can create wod_exercises" ON public.wod_exercises;
DROP POLICY IF EXISTS "Admins can delete wod_exercises" ON public.wod_exercises;
DROP POLICY IF EXISTS "Users can view wod_exercises" ON public.wod_exercises;
DROP POLICY IF EXISTS "Wod owners can create wod exercises" ON public.wod_exercises;
DROP POLICY IF EXISTS "Wod owners can update wod exercises" ON public.wod_exercises;
DROP POLICY IF EXISTS "Wod owners can delete wod exercises" ON public.wod_exercises;

CREATE POLICY "Users can view wod_exercises" ON public.wod_exercises
    FOR SELECT TO authenticated
    USING (
        EXISTS (
            SELECT 1
            FROM public.wods AS w
            WHERE w.id = wod_id
        )
    );

CREATE POLICY "Wod owners can create wod exercises" ON public.wod_exercises
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

CREATE POLICY "Wod owners can update wod exercises" ON public.wod_exercises
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

CREATE POLICY "Wod owners can delete wod exercises" ON public.wod_exercises
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

DROP POLICY IF EXISTS "Trainers can view group results" ON public.results;
DROP POLICY IF EXISTS "Admins can view all results" ON public.results;
DROP POLICY IF EXISTS "Users can view own results" ON public.results;
DROP POLICY IF EXISTS "Users can create own results" ON public.results;
DROP POLICY IF EXISTS "Users can update own results" ON public.results;
DROP POLICY IF EXISTS "Trainers can view managed group results" ON public.results;

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

CREATE POLICY "Users can update own results" ON public.results
    FOR UPDATE TO authenticated
    USING (user_id = (SELECT auth.uid()))
    WITH CHECK (user_id = (SELECT auth.uid()));

DROP POLICY IF EXISTS "Trainers and admins can create classes" ON public.classes;
DROP POLICY IF EXISTS "Trainers can update own classes" ON public.classes;
DROP POLICY IF EXISTS "Admins can delete classes" ON public.classes;
DROP POLICY IF EXISTS "Users can view classes" ON public.classes;
DROP POLICY IF EXISTS "Trainers and admins can update classes" ON public.classes;

CREATE POLICY "Users can view classes" ON public.classes
    FOR SELECT TO authenticated
    USING (
        status = 'scheduled'
        OR public.trainer_owns_class(id)
    );

CREATE POLICY "Trainers and admins can create classes" ON public.classes
    FOR INSERT TO authenticated
    WITH CHECK (
        public.is_admin()
        OR (
            public.current_user_role() = 'trainer'
            AND trainer_id = (SELECT auth.uid())
            AND public.trainer_manages_group(group_id)
        )
    );

CREATE POLICY "Trainers and admins can update classes" ON public.classes
    FOR UPDATE TO authenticated
    USING (
        public.is_admin()
        OR trainer_id = (SELECT auth.uid())
    )
    WITH CHECK (
        public.is_admin()
        OR trainer_id = (SELECT auth.uid())
    );

CREATE POLICY "Admins can delete classes" ON public.classes
    FOR DELETE TO authenticated
    USING (public.is_admin());

DROP POLICY IF EXISTS "Admins can view all bookings" ON public.bookings;
DROP POLICY IF EXISTS "Users can create own bookings" ON public.bookings;
DROP POLICY IF EXISTS "Users can cancel own bookings" ON public.bookings;
DROP POLICY IF EXISTS "Users can view own bookings" ON public.bookings;
DROP POLICY IF EXISTS "Trainers can view class bookings" ON public.bookings;

CREATE POLICY "Users can view own bookings" ON public.bookings
    FOR SELECT TO authenticated
    USING (user_id = (SELECT auth.uid()));

CREATE POLICY "Trainers can view class bookings" ON public.bookings
    FOR SELECT TO authenticated
    USING (public.trainer_owns_class(class_id));

CREATE POLICY "Admins can view all bookings" ON public.bookings
    FOR SELECT TO authenticated
    USING (public.is_admin());

DROP POLICY IF EXISTS "Admins can view all attendance" ON public.attendance;
DROP POLICY IF EXISTS "Trainers can view class attendance" ON public.attendance;
DROP POLICY IF EXISTS "Trainers can mark attendance" ON public.attendance;
DROP POLICY IF EXISTS "Trainers can update attendance" ON public.attendance;

CREATE POLICY "Trainers can view class attendance" ON public.attendance
    FOR SELECT TO authenticated
    USING (public.trainer_owns_class(class_id));

CREATE POLICY "Admins can view all attendance" ON public.attendance
    FOR SELECT TO authenticated
    USING (public.is_admin());

CREATE POLICY "Trainers can mark attendance" ON public.attendance
    FOR INSERT TO authenticated
    WITH CHECK (
        public.trainer_owns_class(class_id)
        AND marked_by = (SELECT auth.uid())
    );

CREATE POLICY "Trainers can update attendance" ON public.attendance
    FOR UPDATE TO authenticated
    USING (public.trainer_owns_class(class_id))
    WITH CHECK (
        public.trainer_owns_class(class_id)
        AND marked_by = (SELECT auth.uid())
    );

DROP POLICY IF EXISTS "Users can view own fcm tokens" ON public.fcm_tokens;
DROP POLICY IF EXISTS "Users can create own fcm tokens" ON public.fcm_tokens;
DROP POLICY IF EXISTS "Users can update own fcm tokens" ON public.fcm_tokens;
DROP POLICY IF EXISTS "Users can delete own fcm tokens" ON public.fcm_tokens;

CREATE POLICY "Users can view own fcm tokens" ON public.fcm_tokens
    FOR SELECT TO authenticated
    USING (user_id = (SELECT auth.uid()));

CREATE POLICY "Users can create own fcm tokens" ON public.fcm_tokens
    FOR INSERT TO authenticated
    WITH CHECK (user_id = (SELECT auth.uid()));

CREATE POLICY "Users can update own fcm tokens" ON public.fcm_tokens
    FOR UPDATE TO authenticated
    USING (user_id = (SELECT auth.uid()))
    WITH CHECK (user_id = (SELECT auth.uid()));

CREATE POLICY "Users can delete own fcm tokens" ON public.fcm_tokens
    FOR DELETE TO authenticated
    USING (user_id = (SELECT auth.uid()));

-- Keep current_bookings consistent even for trusted administrative changes.
CREATE OR REPLACE FUNCTION public.sync_class_booking_count()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
DECLARE
    v_old_confirmed BOOLEAN := false;
    v_new_confirmed BOOLEAN := false;
BEGIN
    IF TG_OP <> 'INSERT' THEN
        v_old_confirmed := OLD.status = 'confirmed';
    END IF;

    IF TG_OP <> 'DELETE' THEN
        v_new_confirmed := NEW.status = 'confirmed';
    END IF;

    IF TG_OP = 'UPDATE' AND OLD.class_id IS DISTINCT FROM NEW.class_id THEN
        IF v_old_confirmed THEN
            UPDATE public.classes
            SET current_bookings = GREATEST(current_bookings - 1, 0)
            WHERE id = OLD.class_id;
        END IF;

        IF v_new_confirmed THEN
            UPDATE public.classes
            SET current_bookings = COALESCE(current_bookings, 0) + 1
            WHERE id = NEW.class_id;
        END IF;
    ELSIF NOT v_old_confirmed AND v_new_confirmed THEN
        UPDATE public.classes
        SET current_bookings = COALESCE(current_bookings, 0) + 1
        WHERE id = NEW.class_id;
    ELSIF v_old_confirmed AND NOT v_new_confirmed THEN
        UPDATE public.classes
        SET current_bookings = GREATEST(current_bookings - 1, 0)
        WHERE id = OLD.class_id;
    END IF;

    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS sync_class_booking_count ON public.bookings;
CREATE TRIGGER sync_class_booking_count
    AFTER INSERT OR DELETE OR UPDATE OF status, class_id ON public.bookings
    FOR EACH ROW
    EXECUTE FUNCTION public.sync_class_booking_count();

-- Recalculate counters before enabling the RPC workflow.
UPDATE public.classes AS c
SET current_bookings = (
    SELECT COUNT(*)::INT
    FROM public.bookings AS b
    WHERE b.class_id = c.id
      AND b.status = 'confirmed'
);

CREATE OR REPLACE FUNCTION public.book_class(p_class_id UUID)
RETURNS public.bookings
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
DECLARE
    v_user_id UUID := (SELECT auth.uid());
    v_class public.classes%ROWTYPE;
    v_booking public.bookings%ROWTYPE;
BEGIN
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Authentication required' USING ERRCODE = '28000';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM public.profiles AS p
        WHERE p.id = v_user_id
          AND p.is_active
    ) THEN
        RAISE EXCEPTION 'Active profile required' USING ERRCODE = '42501';
    END IF;

    SELECT *
    INTO v_class
    FROM public.classes
    WHERE id = p_class_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Class not found' USING ERRCODE = 'P0002';
    END IF;

    IF v_class.status <> 'scheduled' THEN
        RAISE EXCEPTION 'Class is not open for booking' USING ERRCODE = 'P0001';
    END IF;

    SELECT *
    INTO v_booking
    FROM public.bookings
    WHERE class_id = p_class_id
      AND user_id = v_user_id
    FOR UPDATE;

    IF FOUND AND v_booking.status = 'confirmed' THEN
        RETURN v_booking;
    END IF;

    IF COALESCE(v_class.current_bookings, 0) >= COALESCE(v_class.max_capacity, 0) THEN
        RAISE EXCEPTION 'Class capacity reached' USING ERRCODE = 'P0001';
    END IF;

    INSERT INTO public.bookings (
        class_id,
        user_id,
        status,
        booked_at,
        cancelled_at
    )
    VALUES (
        p_class_id,
        v_user_id,
        'confirmed',
        NOW(),
        NULL
    )
    ON CONFLICT (class_id, user_id)
    DO UPDATE SET
        status = 'confirmed',
        booked_at = NOW(),
        cancelled_at = NULL
    RETURNING * INTO v_booking;

    RETURN v_booking;
END;
$$;

CREATE OR REPLACE FUNCTION public.cancel_booking(p_booking_id UUID)
RETURNS public.bookings
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
DECLARE
    v_user_id UUID := (SELECT auth.uid());
    v_class_id UUID;
    v_booking public.bookings%ROWTYPE;
BEGIN
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Authentication required' USING ERRCODE = '28000';
    END IF;

    SELECT b.class_id
    INTO v_class_id
    FROM public.bookings AS b
    WHERE b.id = p_booking_id
      AND b.user_id = v_user_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Booking not found' USING ERRCODE = 'P0002';
    END IF;

    PERFORM 1
    FROM public.classes
    WHERE id = v_class_id
    FOR UPDATE;

    SELECT *
    INTO v_booking
    FROM public.bookings
    WHERE id = p_booking_id
      AND user_id = v_user_id
    FOR UPDATE;

    IF v_booking.status = 'cancelled' THEN
        RETURN v_booking;
    END IF;

    UPDATE public.bookings
    SET status = 'cancelled',
        cancelled_at = NOW()
    WHERE id = p_booking_id
    RETURNING * INTO v_booking;

    RETURN v_booking;
END;
$$;

REVOKE ALL ON FUNCTION public.book_class(UUID) FROM PUBLIC;
REVOKE ALL ON FUNCTION public.cancel_booking(UUID) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.book_class(UUID) TO authenticated;
GRANT EXECUTE ON FUNCTION public.cancel_booking(UUID) TO authenticated;

COMMIT;
