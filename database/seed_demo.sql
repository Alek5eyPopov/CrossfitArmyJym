-- ============================================================
-- CrossFit ARMY demo data
-- ============================================================
-- Run only in a development or staging Supabase project.
-- The script requires at least one active trainer profile and one active
-- athlete profile. It never creates auth.users and never stores passwords.
--
-- Optional: replace NULL values below with exact account emails to choose
-- which existing profiles are used by the demo dataset.
-- ============================================================

BEGIN;

CREATE TEMP TABLE seed_demo_config (
    trainer_email TEXT,
    athlete_email TEXT
) ON COMMIT DROP;

INSERT INTO seed_demo_config (trainer_email, athlete_email)
VALUES (
    NULL, -- Example: 'trainer-test@example.com'
    NULL  -- Example: 'athlete-test@example.com'
);

-- SQL Editor runs as a trusted database owner, but the profile protection
-- trigger is intentionally designed for JWT requests. Disable only that
-- trigger inside this transaction while assigning demo groups.
ALTER TABLE public.profiles
DISABLE TRIGGER protect_profile_privileged_fields;

DO $$
DECLARE
    v_trainer_id UUID;
    v_athlete_id UUID;
    v_group_morning UUID := '10000000-0000-0000-0000-000000000001';
    v_group_evening UUID := '10000000-0000-0000-0000-000000000002';
    v_ex_burpee UUID := '20000000-0000-0000-0000-000000000001';
    v_ex_pull_up UUID := '20000000-0000-0000-0000-000000000002';
    v_ex_thruster UUID := '20000000-0000-0000-0000-000000000003';
    v_ex_run UUID := '20000000-0000-0000-0000-000000000004';
    v_ex_row UUID := '20000000-0000-0000-0000-000000000005';
    v_ex_deadlift UUID := '20000000-0000-0000-0000-000000000006';
    v_wod_today UUID := '30000000-0000-0000-0000-000000000001';
    v_wod_yesterday UUID := '30000000-0000-0000-0000-000000000002';
    v_wod_week UUID := '30000000-0000-0000-0000-000000000003';
    v_class_past UUID := '40000000-0000-0000-0000-000000000001';
    v_class_morning UUID := '40000000-0000-0000-0000-000000000002';
    v_class_evening UUID := '40000000-0000-0000-0000-000000000003';
    v_class_tomorrow UUID := '40000000-0000-0000-0000-000000000004';
    v_class_weekend UUID := '40000000-0000-0000-0000-000000000005';
    v_athlete UUID;
    v_index INTEGER := 0;
BEGIN
    SELECT p.id
    INTO v_trainer_id
    FROM public.profiles AS p
    CROSS JOIN seed_demo_config AS c
    WHERE p.role = 'trainer'
      AND p.is_active
      AND (c.trainer_email IS NULL OR p.email = c.trainer_email)
    ORDER BY p.created_at
    LIMIT 1;

    IF v_trainer_id IS NULL THEN
        RAISE EXCEPTION
            'Demo seed requires an active trainer profile. '
            'Create a user and set profiles.role = trainer first.';
    END IF;

    SELECT p.id
    INTO v_athlete_id
    FROM public.profiles AS p
    CROSS JOIN seed_demo_config AS c
    WHERE p.role = 'athlete'
      AND p.is_active
      AND (c.athlete_email IS NULL OR p.email = c.athlete_email)
    ORDER BY p.created_at
    LIMIT 1;

    IF v_athlete_id IS NULL THEN
        RAISE EXCEPTION
            'Demo seed requires an active athlete profile. '
            'Register at least one athlete account first.';
    END IF;

    -- Remove only the deterministic demo dataset from a previous run.
    DELETE FROM public.attendance
    WHERE class_id IN (
        v_class_past,
        v_class_morning,
        v_class_evening,
        v_class_tomorrow,
        v_class_weekend
    );

    DELETE FROM public.bookings
    WHERE class_id IN (
        v_class_past,
        v_class_morning,
        v_class_evening,
        v_class_tomorrow,
        v_class_weekend
    );

    DELETE FROM public.results
    WHERE wod_id IN (v_wod_today, v_wod_yesterday, v_wod_week);

    DELETE FROM public.classes
    WHERE id IN (
        v_class_past,
        v_class_morning,
        v_class_evening,
        v_class_tomorrow,
        v_class_weekend
    );

    DELETE FROM public.wod_exercises
    WHERE wod_id IN (v_wod_today, v_wod_yesterday, v_wod_week);

    DELETE FROM public.wods
    WHERE id IN (v_wod_today, v_wod_yesterday, v_wod_week);

    DELETE FROM public.exercises
    WHERE id IN (
        v_ex_burpee,
        v_ex_pull_up,
        v_ex_thruster,
        v_ex_run,
        v_ex_row,
        v_ex_deadlift
    );

    -- Groups are upserted because existing athlete profiles may still point to them.
    INSERT INTO public.groups (
        id,
        name,
        trainer_id,
        schedule,
        is_active
    )
    VALUES
        (
            v_group_morning,
            'ARMY Утро',
            v_trainer_id,
            'Пн, Ср, Пт — 07:00',
            true
        ),
        (
            v_group_evening,
            'ARMY Вечер',
            v_trainer_id,
            'Вт, Чт — 19:00; Сб — 11:00',
            true
        )
    ON CONFLICT (id)
    DO UPDATE SET
        name = EXCLUDED.name,
        trainer_id = EXCLUDED.trainer_id,
        schedule = EXCLUDED.schedule,
        is_active = EXCLUDED.is_active;

    -- Put all active demo athletes into two visible groups.
    v_index := 0;
    FOR v_athlete IN
        SELECT p.id
        FROM public.profiles AS p
        WHERE p.role = 'athlete'
          AND p.is_active
        ORDER BY
            CASE WHEN p.id = v_athlete_id THEN 0 ELSE 1 END,
            p.created_at
    LOOP
        UPDATE public.profiles
        SET group_id = CASE
                WHEN v_index % 2 = 0 THEN v_group_morning
                ELSE v_group_evening
            END,
            updated_at = NOW()
        WHERE id = v_athlete;

        v_index := v_index + 1;
    END LOOP;

    INSERT INTO public.exercises (
        id,
        name,
        category,
        description,
        difficulty,
        unit_type,
        created_by
    )
    VALUES
        (
            v_ex_burpee,
            'Burpee',
            'gymnastics',
            'Бёрпи с касанием грудью пола и прыжком.',
            'beginner',
            'reps',
            v_trainer_id
        ),
        (
            v_ex_pull_up,
            'Pull-up',
            'gymnastics',
            'Подтягивания строгие или киппинг.',
            'intermediate',
            'reps',
            v_trainer_id
        ),
        (
            v_ex_thruster,
            'Thruster',
            'weightlifting',
            'Фронтальный присед с выбросом штанги над головой.',
            'intermediate',
            'kg',
            v_trainer_id
        ),
        (
            v_ex_run,
            'Run',
            'cardio',
            'Бег на улице или дорожке.',
            'beginner',
            'meters',
            v_trainer_id
        ),
        (
            v_ex_row,
            'Row',
            'monostructural',
            'Гребля на Concept2.',
            'beginner',
            'meters',
            v_trainer_id
        ),
        (
            v_ex_deadlift,
            'Deadlift',
            'weightlifting',
            'Классическая становая тяга.',
            'intermediate',
            'kg',
            v_trainer_id
        );

    INSERT INTO public.wods (
        id,
        name,
        format,
        target_group_id,
        trainer_id,
        scheduled_date,
        time_cap_seconds,
        notes
    )
    VALUES
        (
            v_wod_today,
            'ARMY POWER',
            'amrap',
            v_group_morning,
            v_trainer_id,
            CURRENT_DATE,
            1200,
            'AMRAP 20: контролируйте темп и сохраняйте технику.'
        ),
        (
            v_wod_yesterday,
            'RED LINE',
            'for_time',
            v_group_morning,
            v_trainer_id,
            CURRENT_DATE - 1,
            900,
            'Завершить комплекс как можно быстрее. Time cap 15 минут.'
        ),
        (
            v_wod_week,
            'SATURDAY TEAM',
            'emom',
            v_group_evening,
            v_trainer_id,
            CURRENT_DATE + 3,
            1440,
            'Командный EMOM 24 минуты.'
        );

    INSERT INTO public.wod_exercises (
        id,
        wod_id,
        exercise_id,
        rounds,
        recommended_weight_kg,
        custom_instruction
    )
    VALUES
        (
            '31000000-0000-0000-0000-000000000001',
            v_wod_today,
            v_ex_burpee,
            10,
            0,
            '10 повторений'
        ),
        (
            '31000000-0000-0000-0000-000000000002',
            v_wod_today,
            v_ex_thruster,
            12,
            35,
            '12 повторений, вес масштабируется'
        ),
        (
            '31000000-0000-0000-0000-000000000003',
            v_wod_today,
            v_ex_row,
            1,
            0,
            '250 метров'
        ),
        (
            '31000000-0000-0000-0000-000000000004',
            v_wod_yesterday,
            v_ex_run,
            1,
            0,
            '800 метров'
        ),
        (
            '31000000-0000-0000-0000-000000000005',
            v_wod_yesterday,
            v_ex_pull_up,
            30,
            0,
            '30 повторений'
        ),
        (
            '31000000-0000-0000-0000-000000000006',
            v_wod_yesterday,
            v_ex_deadlift,
            20,
            70,
            '20 повторений'
        ),
        (
            '31000000-0000-0000-0000-000000000007',
            v_wod_week,
            v_ex_row,
            1,
            0,
            '12 калорий'
        ),
        (
            '31000000-0000-0000-0000-000000000008',
            v_wod_week,
            v_ex_burpee,
            10,
            0,
            '10 синхронных повторений'
        );

    INSERT INTO public.classes (
        id,
        group_id,
        trainer_id,
        wod_id,
        scheduled_start,
        scheduled_end,
        max_capacity,
        current_bookings,
        location,
        status
    )
    VALUES
        (
            v_class_past,
            v_group_morning,
            v_trainer_id,
            v_wod_yesterday,
            (CURRENT_DATE - 1) + TIME '18:00',
            (CURRENT_DATE - 1) + TIME '19:00',
            12,
            0,
            'Основной зал',
            'completed'
        ),
        (
            v_class_morning,
            v_group_morning,
            v_trainer_id,
            v_wod_today,
            (CURRENT_DATE + 1) + TIME '07:00',
            (CURRENT_DATE + 1) + TIME '08:00',
            12,
            0,
            'Основной зал',
            'scheduled'
        ),
        (
            v_class_evening,
            v_group_morning,
            v_trainer_id,
            v_wod_today,
            (CURRENT_DATE + 1) + TIME '19:00',
            (CURRENT_DATE + 1) + TIME '20:00',
            10,
            0,
            'Основной зал',
            'scheduled'
        ),
        (
            v_class_tomorrow,
            v_group_evening,
            v_trainer_id,
            v_wod_week,
            (CURRENT_DATE + 2) + TIME '18:30',
            (CURRENT_DATE + 2) + TIME '19:30',
            16,
            0,
            'Малый зал',
            'scheduled'
        ),
        (
            v_class_weekend,
            v_group_evening,
            v_trainer_id,
            v_wod_week,
            (CURRENT_DATE + 3) + TIME '11:00',
            (CURRENT_DATE + 3) + TIME '12:15',
            20,
            0,
            'Основной зал',
            'scheduled'
        );

    -- Add bookings for all active athletes. The trigger updates current_bookings.
    v_index := 0;
    FOR v_athlete IN
        SELECT p.id
        FROM public.profiles AS p
        WHERE p.role = 'athlete'
          AND p.is_active
        ORDER BY
            CASE WHEN p.id = v_athlete_id THEN 0 ELSE 1 END,
            p.created_at
    LOOP
        INSERT INTO public.bookings (
            id,
            class_id,
            user_id,
            booked_at,
            status
        )
        VALUES (
            (
                '50000000-0000-0000-0000-'
                || LPAD((v_index + 1)::TEXT, 12, '0')
            )::UUID,
            v_class_past,
            v_athlete,
            NOW() - INTERVAL '2 days',
            'confirmed'
        );

        IF v_index < 10 THEN
            INSERT INTO public.bookings (
                id,
                class_id,
                user_id,
                booked_at,
                status
            )
            VALUES (
                (
                    '51000000-0000-0000-0000-'
                    || LPAD((v_index + 1)::TEXT, 12, '0')
                )::UUID,
                v_class_morning,
                v_athlete,
                NOW() - INTERVAL '1 day',
                'confirmed'
            );
        END IF;

        IF v_index < 7 THEN
            INSERT INTO public.bookings (
                id,
                class_id,
                user_id,
                booked_at,
                status
            )
            VALUES (
                (
                    '52000000-0000-0000-0000-'
                    || LPAD((v_index + 1)::TEXT, 12, '0')
                )::UUID,
                v_class_evening,
                v_athlete,
                NOW() - INTERVAL '12 hours',
                'confirmed'
            );
        END IF;

        IF v_index < 5 THEN
            INSERT INTO public.bookings (
                id,
                class_id,
                user_id,
                booked_at,
                status
            )
            VALUES (
                (
                    '53000000-0000-0000-0000-'
                    || LPAD((v_index + 1)::TEXT, 12, '0')
                )::UUID,
                v_class_tomorrow,
                v_athlete,
                NOW() - INTERVAL '6 hours',
                'confirmed'
            );
        END IF;

        INSERT INTO public.attendance (
            id,
            class_id,
            user_id,
            attended,
            check_in_time,
            marked_by,
            notes
        )
        VALUES (
            (
                '60000000-0000-0000-0000-'
                || LPAD((v_index + 1)::TEXT, 12, '0')
            )::UUID,
            v_class_past,
            v_athlete,
            v_index % 4 <> 3,
            CASE
                WHEN v_index % 4 <> 3
                    THEN (CURRENT_DATE - 1) + TIME '17:55'
                ELSE NULL
            END,
            v_trainer_id,
            CASE
                WHEN v_index % 4 = 3 THEN 'Предупредил об отсутствии'
                ELSE NULL
            END
        );

        INSERT INTO public.results (
            id,
            wod_id,
            user_id,
            score,
            formatted_score,
            completed_at,
            is_pr,
            synced_at
        )
        VALUES
            (
                (
                    '70000000-0000-0000-0000-'
                    || LPAD((v_index + 1)::TEXT, 12, '0')
                )::UUID,
                v_wod_yesterday,
                v_athlete,
                520 + v_index * 13,
                TO_CHAR(
                    (TIME '00:00:00' + (520 + v_index * 13) * INTERVAL '1 second'),
                    'MI:SS'
                ),
                NOW() - INTERVAL '1 day',
                true,
                NOW()
            ),
            (
                (
                    '71000000-0000-0000-0000-'
                    || LPAD((v_index + 1)::TEXT, 12, '0')
                )::UUID,
                v_wod_today,
                v_athlete,
                8 + v_index,
                (8 + v_index)::TEXT || ' раундов',
                NOW() - INTERVAL '2 hours',
                true,
                NOW()
            );

        v_index := v_index + 1;
    END LOOP;

    RAISE NOTICE
        'Demo seed complete. Trainer: %, primary athlete: %, athletes: %',
        v_trainer_id,
        v_athlete_id,
        v_index;
END;
$$;

ALTER TABLE public.profiles
ENABLE TRIGGER protect_profile_privileged_fields;

COMMIT;

-- Quick verification summary.
SELECT 'groups' AS entity, COUNT(*) AS demo_rows
FROM public.groups
WHERE id::TEXT LIKE '10000000-%'
UNION ALL
SELECT 'exercises', COUNT(*)
FROM public.exercises
WHERE id::TEXT LIKE '20000000-%'
UNION ALL
SELECT 'wods', COUNT(*)
FROM public.wods
WHERE id::TEXT LIKE '30000000-%'
UNION ALL
SELECT 'classes', COUNT(*)
FROM public.classes
WHERE id::TEXT LIKE '40000000-%'
UNION ALL
SELECT 'bookings', COUNT(*)
FROM public.bookings
WHERE id::TEXT LIKE '5%'
UNION ALL
SELECT 'attendance', COUNT(*)
FROM public.attendance
WHERE id::TEXT LIKE '60000000-%'
UNION ALL
SELECT 'results', COUNT(*)
FROM public.results
WHERE id::TEXT LIKE '7%';
