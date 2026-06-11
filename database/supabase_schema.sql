-- ============================================================
-- CrossFit Gym Manager - Supabase Database Schema
-- ============================================================
-- Этот скрипт создает все необходимые таблицы, индексы, 
-- RLS-политики и триггеры для работы приложения.
-- После первого запуска обязательно примените миграции из database/migrations
-- в порядке их имён. Миграция 20260611_stage2_security.sql заменяет начальные
-- политики на безопасные и добавляет атомарную запись на занятия.
-- ============================================================

-- ============================================================
-- 1. Создание таблиц
-- ============================================================

-- Таблица профилей пользователей (расширяет auth.users)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT UNIQUE NOT NULL,
    full_name TEXT NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('athlete', 'trainer', 'admin')),
    group_id UUID,
    avatar_url TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Таблица тренировочных групп
CREATE TABLE IF NOT EXISTS public.groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    trainer_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    schedule TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Добавляем внешнее ключ для group_id в profiles
ALTER TABLE public.profiles 
ADD CONSTRAINT fk_profiles_group 
FOREIGN KEY (group_id) REFERENCES public.groups(id) ON DELETE SET NULL;

-- Таблица упражнений
CREATE TABLE IF NOT EXISTS public.exercises (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    category TEXT NOT NULL CHECK (category IN ('gymnastics', 'weightlifting', 'cardio', 'monostructural')),
    description TEXT,
    video_url TEXT,
    difficulty TEXT CHECK (difficulty IN ('beginner', 'intermediate', 'advanced')),
    unit_type TEXT NOT NULL,
    created_by UUID REFERENCES public.profiles(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Таблица WOD (тренировок дня)
CREATE TABLE IF NOT EXISTS public.wods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    format TEXT NOT NULL CHECK (format IN ('amrap', 'emom', 'for_time', 'tabata', 'ladder')),
    target_group_id UUID REFERENCES public.groups(id) ON DELETE CASCADE,
    trainer_id UUID REFERENCES public.profiles(id) NOT NULL,
    scheduled_date DATE NOT NULL,
    time_cap_seconds INT DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Таблица связи WOD и упражнений
CREATE TABLE IF NOT EXISTS public.wod_exercises (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wod_id UUID REFERENCES public.wods(id) ON DELETE CASCADE,
    exercise_id UUID REFERENCES public.exercises(id) ON DELETE CASCADE,
    rounds INT DEFAULT 1,
    recommended_weight_kg INT DEFAULT 0,
    custom_instruction TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(wod_id, exercise_id)
);

-- Таблица результатов тренировок
CREATE TABLE IF NOT EXISTS public.results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wod_id UUID REFERENCES public.wods(id) ON DELETE CASCADE,
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    score NUMERIC NOT NULL,
    formatted_score TEXT,
    completed_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    is_pr BOOLEAN DEFAULT false,
    synced_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Таблица расписания занятий (classes/schedule)
CREATE TABLE IF NOT EXISTS public.classes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID REFERENCES public.groups(id) ON DELETE CASCADE,
    trainer_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    wod_id UUID REFERENCES public.wods(id) ON DELETE SET NULL,
    scheduled_start TIMESTAMP NOT NULL,
    scheduled_end TIMESTAMP NOT NULL,
    max_capacity INT DEFAULT 20,
    current_bookings INT DEFAULT 0,
    location TEXT DEFAULT 'Main Box',
    status TEXT DEFAULT 'scheduled' CHECK (status IN ('scheduled', 'cancelled', 'completed')),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Таблица записей клиентов на занятия
CREATE TABLE IF NOT EXISTS public.bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_id UUID REFERENCES public.classes(id) ON DELETE CASCADE,
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    booked_at TIMESTAMP DEFAULT NOW(),
    status TEXT DEFAULT 'confirmed' CHECK (status IN ('confirmed', 'cancelled', 'waitlisted')),
    cancelled_at TIMESTAMP,
    UNIQUE(class_id, user_id)
);

-- Таблица посещаемости
CREATE TABLE IF NOT EXISTS public.attendance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_id UUID REFERENCES public.classes(id) ON DELETE CASCADE,
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    attended BOOLEAN DEFAULT false,
    check_in_time TIMESTAMP,
    marked_by UUID REFERENCES public.profiles(id),
    notes TEXT,
    UNIQUE(class_id, user_id)
);

-- Таблица FCM токенов для push-уведомлений
CREATE TABLE IF NOT EXISTS public.fcm_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    token TEXT UNIQUE NOT NULL,
    device_name TEXT,
    last_used TIMESTAMP DEFAULT NOW(),
    created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- 2. Создание индексов
-- ============================================================

-- Индексы для profiles
CREATE INDEX IF NOT EXISTS idx_profiles_role ON public.profiles(role);
CREATE INDEX IF NOT EXISTS idx_profiles_group ON public.profiles(group_id);
CREATE INDEX IF NOT EXISTS idx_profiles_email ON public.profiles(email);
CREATE INDEX IF NOT EXISTS idx_profiles_is_active ON public.profiles(is_active);

-- Индексы для groups
CREATE INDEX IF NOT EXISTS idx_groups_trainer ON public.groups(trainer_id);
CREATE INDEX IF NOT EXISTS idx_groups_is_active ON public.groups(is_active);

-- Индексы для exercises
CREATE INDEX IF NOT EXISTS idx_exercises_category ON public.exercises(category);
CREATE INDEX IF NOT EXISTS idx_exercises_created_by ON public.exercises(created_by);
CREATE INDEX IF NOT EXISTS idx_exercises_difficulty ON public.exercises(difficulty);

-- Индексы для wods
CREATE INDEX IF NOT EXISTS idx_wods_date ON public.wods(scheduled_date);
CREATE INDEX IF NOT EXISTS idx_wods_group ON public.wods(target_group_id);
CREATE INDEX IF NOT EXISTS idx_wods_trainer ON public.wods(trainer_id);

-- Индексы для wod_exercises
CREATE INDEX IF NOT EXISTS idx_wod_exercises_wod ON public.wod_exercises(wod_id);
CREATE INDEX IF NOT EXISTS idx_wod_exercises_exercise ON public.wod_exercises(exercise_id);

-- Индексы для results
CREATE INDEX IF NOT EXISTS idx_results_wod ON public.results(wod_id);
CREATE INDEX IF NOT EXISTS idx_results_user ON public.results(user_id);
CREATE INDEX IF NOT EXISTS idx_results_completed ON public.results(completed_at);
CREATE INDEX IF NOT EXISTS idx_results_is_pr ON public.results(is_pr) WHERE is_pr = true;

-- Индексы для classes
CREATE INDEX IF NOT EXISTS idx_classes_date ON public.classes(scheduled_start);
CREATE INDEX IF NOT EXISTS idx_classes_trainer ON public.classes(trainer_id);
CREATE INDEX IF NOT EXISTS idx_classes_group ON public.classes(group_id);

-- Индексы для bookings
CREATE INDEX IF NOT EXISTS idx_bookings_user ON public.bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_class ON public.bookings(class_id);

-- Индексы для attendance
CREATE INDEX IF NOT EXISTS idx_attendance_class ON public.attendance(class_id);

-- Индексы для fcm_tokens
CREATE INDEX IF NOT EXISTS idx_fcm_user ON public.fcm_tokens(user_id);

-- ============================================================
-- 3. RLS (Row Level Security) политики
-- ============================================================

-- Включаем RLS для всех таблиц
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.groups ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.exercises ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.wods ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.wod_exercises ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.results ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.classes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.bookings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.attendance ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.fcm_tokens ENABLE ROW LEVEL SECURITY;

-- ---- Политики для profiles ----

-- Любой авторизованный пользователь может читать свой профиль
CREATE POLICY "Users can view own profile" ON public.profiles
    FOR SELECT USING (auth.uid() = id);

-- Атлеты могут читать профили других пользователей в своей группе
CREATE POLICY "Athletes can view group profiles" ON public.profiles
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() 
            AND p.group_id = public.profiles.group_id
        )
    );

-- Тренеры могут читать все профили
CREATE POLICY "Trainers can view all profiles" ON public.profiles
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND p.role = 'trainer'
        )
    );

-- Админы могут читать, создавать, обновлять и удалять все профили
CREATE POLICY "Admins full access profiles" ON public.profiles
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND p.role = 'admin'
        )
    ) WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND p.role = 'admin'
        )
    );

-- Пользователи могут обновлять свой профиль
CREATE POLICY "Users can update own profile" ON public.profiles
    FOR UPDATE USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

-- ---- Политики для groups ----

-- Все авторизованные пользователи могут читать активные группы
CREATE POLICY "Users can view active groups" ON public.groups
    FOR SELECT USING (is_active = true);

-- Тренеры могут создавать группы
CREATE POLICY "Trainers can create groups" ON public.groups
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND p.role IN ('trainer', 'admin')
        )
    );

-- Тренеры могут обновлять только свои группы
CREATE POLICY "Trainers can update own groups" ON public.groups
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND (p.role = 'admin' OR p.id = trainer_id)
        )
    ) WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND (p.role = 'admin' OR p.id = trainer_id)
        )
    );

-- Админы могут удалять группы
CREATE POLICY "Admins can delete groups" ON public.groups
    FOR DELETE USING (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND p.role = 'admin'
        )
    );

-- ---- Политики для exercises ----

-- Все авторизованные пользователи могут читать упражнения
CREATE POLICY "Users can view exercises" ON public.exercises
    FOR SELECT USING (true);

-- Тренеры и админы могут создавать упражнения
CREATE POLICY "Trainers and admins can create exercises" ON public.exercises
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND p.role IN ('trainer', 'admin')
        )
    );

-- Авторы могут обновлять свои упражнения, админы - все
CREATE POLICY "Authors can update own exercises" ON public.exercises
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND (p.role = 'admin' OR p.id = created_by)
        )
    ) WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND (p.role = 'admin' OR p.id = created_by)
        )
    );

-- Админы могут удалять упражнения
CREATE POLICY "Admins can delete exercises" ON public.exercises
    FOR DELETE USING (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND p.role = 'admin'
        )
    );

-- ---- Политики для wods ----

-- Атлеты могут читать WOD своей группы
CREATE POLICY "Athletes can view group wods" ON public.wods
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.profiles p
            JOIN public.groups g ON g.id = p.group_id
            WHERE p.id = auth.uid() 
            AND (g.id = target_group_id OR target_group_id IS NULL)
        )
    );

-- Тренеры могут читать все WOD
CREATE POLICY "Trainers can view all wods" ON public.wods
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND p.role IN ('trainer', 'admin')
        )
    );

-- Тренеры могут создавать WOD
CREATE POLICY "Trainers can create wods" ON public.wods
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND p.role IN ('trainer', 'admin')
        )
    );

-- Тренеры могут обновлять свои WOD, админы - все
CREATE POLICY "Trainers can update own wods" ON public.wods
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND (p.role = 'admin' OR p.id = trainer_id)
        )
    ) WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND (p.role = 'admin' OR p.id = trainer_id)
        )
    );

-- Админы могут удалять WOD
CREATE POLICY "Admins can delete wods" ON public.wods
    FOR DELETE USING (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND p.role = 'admin'
        )
    );

-- ---- Политики для wod_exercises ----

-- Все авторизованные пользователи могут читать wod_exercises
CREATE POLICY "Users can view wod_exercises" ON public.wod_exercises
    FOR SELECT USING (true);

-- Тренеры и админы могут создавать wod_exercises
CREATE POLICY "Trainers can create wod_exercises" ON public.wod_exercises
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND p.role IN ('trainer', 'admin')
        )
    );

-- Админы могут удалять wod_exercises
CREATE POLICY "Admins can delete wod_exercises" ON public.wod_exercises
    FOR DELETE USING (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND p.role = 'admin'
        )
    );

-- ---- Политики для results ----

-- Атлеты могут читать свои результаты
CREATE POLICY "Users can view own results" ON public.results
    FOR SELECT USING (user_id = auth.uid());

-- Тренеры могут читать результаты атлетов своей группы
CREATE POLICY "Trainers can view group results" ON public.results
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.profiles p
            JOIN public.groups g ON g.id = p.group_id
            WHERE p.id = auth.uid() AND p.role = 'trainer'
        ) AND EXISTS (
            SELECT 1 FROM public.profiles up
            WHERE up.id = user_id AND up.group_id = (
                SELECT group_id FROM public.profiles WHERE id = auth.uid()
            )
        )
    );

-- Админы могут читать все результаты
CREATE POLICY "Admins can view all results" ON public.results
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND p.role = 'admin'
        )
    );

-- Атлеты могут создавать свои результаты
CREATE POLICY "Users can create own results" ON public.results
    FOR INSERT WITH CHECK (user_id = auth.uid());

-- Атлеты могут обновлять свои результаты
CREATE POLICY "Users can update own results" ON public.results
    FOR UPDATE USING (user_id = auth.uid())
    WITH CHECK (user_id = auth.uid());

-- ---- Политики для classes ----

-- Все авторизованные пользователи могут читать активные занятия
CREATE POLICY "Users can view classes" ON public.classes
    FOR SELECT USING (status = 'scheduled');

-- Тренеры и админы могут создавать занятия
CREATE POLICY "Trainers and admins can create classes" ON public.classes
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND p.role IN ('trainer', 'admin')
        )
    );

-- Тренеры могут обновлять свои занятия, админы - все
CREATE POLICY "Trainers can update own classes" ON public.classes
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND (p.role = 'admin' OR p.id = trainer_id)
        )
    ) WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND (p.role = 'admin' OR p.id = trainer_id)
        )
    );

-- Админы могут удалять занятия
CREATE POLICY "Admins can delete classes" ON public.classes
    FOR DELETE USING (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND p.role = 'admin'
        )
    );

-- ---- Политики для bookings ----

-- Атлеты могут читать свои записи
CREATE POLICY "Users can view own bookings" ON public.bookings
    FOR SELECT USING (user_id = auth.uid());

-- Тренеры могут читать записи на свои занятия
CREATE POLICY "Trainers can view class bookings" ON public.bookings
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.classes c
            WHERE c.id = class_id AND c.trainer_id = auth.uid()
        )
    );

-- Админы могут читать все записи
CREATE POLICY "Admins can view all bookings" ON public.bookings
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND p.role = 'admin'
        )
    );

-- Атлеты могут создавать свои записи (если не записаны и есть места)
CREATE POLICY "Users can create own bookings" ON public.bookings
    FOR INSERT WITH CHECK (
        user_id = auth.uid() AND
        EXISTS (
            SELECT 1 FROM public.classes c
            WHERE c.id = class_id AND c.current_bookings < c.max_capacity
        )
    );

-- Атлеты могут отменять свои записи (только confirmed -> cancelled)
CREATE POLICY "Users can cancel own bookings" ON public.bookings
    FOR UPDATE USING (user_id = auth.uid() AND status = 'confirmed')
    WITH CHECK (user_id = auth.uid() AND status = 'cancelled');

-- ---- Политики для attendance ----

-- Тренеры могут читать посещаемость своих занятий
CREATE POLICY "Trainers can view class attendance" ON public.attendance
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.classes c
            WHERE c.id = class_id AND c.trainer_id = auth.uid()
        )
    );

-- Админы могут читать всю посещаемость
CREATE POLICY "Admins can view all attendance" ON public.attendance
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.profiles p 
            WHERE p.id = auth.uid() AND p.role = 'admin'
        )
    );

-- Тренеры могут отмечать посещаемость на своих занятиях
CREATE POLICY "Trainers can mark attendance" ON public.attendance
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.classes c
            WHERE c.id = class_id AND c.trainer_id = auth.uid()
        )
    );

-- Тренеры могут обновлять посещаемость на своих занятиях
CREATE POLICY "Trainers can update attendance" ON public.attendance
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM public.classes c
            WHERE c.id = class_id AND c.trainer_id = auth.uid()
        )
    ) WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.classes c
            WHERE c.id = class_id AND c.trainer_id = auth.uid()
        )
    );

-- ---- Политики для fcm_tokens ----

-- Пользователи могут читать свои токены
CREATE POLICY "Users can view own fcm tokens" ON public.fcm_tokens
    FOR SELECT USING (user_id = auth.uid());

-- Пользователи могут создавать свои токены
CREATE POLICY "Users can create own fcm tokens" ON public.fcm_tokens
    FOR INSERT WITH CHECK (user_id = auth.uid());

-- Пользователи могут обновлять свои токены
CREATE POLICY "Users can update own fcm tokens" ON public.fcm_tokens
    FOR UPDATE USING (user_id = auth.uid())
    WITH CHECK (user_id = auth.uid());

-- Пользователи могут удалять свои токены
CREATE POLICY "Users can delete own fcm tokens" ON public.fcm_tokens
    FOR DELETE USING (user_id = auth.uid());

-- ============================================================
-- 4. Триггерная функция для автоматического создания профиля
-- ============================================================

-- Функция для создания профиля при регистрации
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, email, full_name, role, is_active)
    VALUES (
        NEW.id,
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'full_name', split_part(NEW.email, '@', 1)),
        'athlete',
        true
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Триггер для автоматического создания профиля
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_user();

-- ============================================================
-- 5. Функция для обновления updated_at
-- ============================================================

CREATE OR REPLACE FUNCTION public.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_profiles_updated_at
    BEFORE UPDATE ON public.profiles
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at_column();

-- ============================================================
-- 6. Начальные данные (опционально)
-- ============================================================

-- Вставьте начальные упражнения (пример)
-- INSERT INTO public.exercises (name, category, description, difficulty, unit_type) VALUES
-- ('Push-ups', 'gymnastics', 'Standard push-up exercise', 'beginner', 'reps'),
-- ('Pull-ups', 'gymnastics', 'Standard pull-up exercise', 'intermediate', 'reps'),
-- ('Squats', 'weightlifting', 'Barbell back squat', 'beginner', 'reps'),
-- ('Deadlift', 'weightlifting', 'Conventional deadlift', 'intermediate', 'kg'),
-- ('Running', 'cardio', 'Outdoor or treadmill running', 'beginner', 'meters'),
-- ('Rowing', 'cardio', 'Rowing machine', 'beginner', 'meters');

-- ============================================================
-- Конец скрипта
-- ============================================================
