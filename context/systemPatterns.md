## Architecture, design patterns, component relationships
Подробное описание таблиц
1. profiles (расширение auth.users)
   sql
   -- Связи: belongs_to groups (many-to-one)
   -- Поля ролей определяют доступ к другим таблицам
   CREATE TABLE public.profiles (
   id UUID PRIMARY KEY REFERENCES auth.users(id),
   email TEXT UNIQUE NOT NULL,
   full_name TEXT NOT NULL,
   role TEXT NOT NULL CHECK (role IN ('athlete', 'trainer', 'admin')),
   group_id UUID REFERENCES public.groups(id) ON DELETE SET NULL,
   avatar_url TEXT,
   is_active BOOLEAN DEFAULT true,
   created_at TIMESTAMP DEFAULT NOW(),
   updated_at TIMESTAMP DEFAULT NOW(),

   -- Индексы для быстрого поиска
   INDEX idx_profiles_role (role),
   INDEX idx_profiles_group (group_id)
   );
2. groups (тренировочные группы)
   sql
   CREATE TABLE public.groups (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   name TEXT NOT NULL,
   trainer_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
   schedule TEXT, -- "ПН,СР,ПТ 08:00-09:00"
   is_active BOOLEAN DEFAULT true,
   created_at TIMESTAMP DEFAULT NOW(),

   INDEX idx_groups_trainer (trainer_id)
   );
3. exercises (база упражнений)
   sql
   CREATE TABLE public.exercises (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   name TEXT NOT NULL,
   category TEXT NOT NULL CHECK (category IN ('gymnastics', 'weightlifting', 'cardio', 'monostructural')),
   description TEXT,
   video_url TEXT,
   difficulty TEXT CHECK (difficulty IN ('beginner', 'intermediate', 'advanced')),
   unit_type TEXT NOT NULL, -- 'kg', 'reps', 'seconds', 'calories', 'meters'
   created_by UUID REFERENCES public.profiles(id),
   created_at TIMESTAMP DEFAULT NOW(),

   INDEX idx_exercises_category (category)
   );
4. wods (тренировки дня)
   sql
   CREATE TABLE public.wods (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   name TEXT NOT NULL,
   format TEXT NOT NULL CHECK (format IN ('amrap', 'emom', 'for_time', 'tabata', 'ladder')),
   target_group_id UUID REFERENCES public.groups(id) ON DELETE CASCADE,
   trainer_id UUID REFERENCES public.profiles(id) NOT NULL,
   scheduled_date DATE NOT NULL,
   time_cap_seconds INT DEFAULT 0,
   notes TEXT,
   created_at TIMESTAMP DEFAULT NOW(),

   INDEX idx_wods_date (scheduled_date),
   INDEX idx_wods_group (target_group_id),
   INDEX idx_wods_trainer (trainer_id)
   );
5. wod_exercises (связь WOD → упражнения)
   sql
   -- many-to-many с дополнительными атрибутами
   CREATE TABLE public.wod_exercises (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   wod_id UUID REFERENCES public.wods(id) ON DELETE CASCADE,
   exercise_id UUID REFERENCES public.exercises(id) ON DELETE CASCADE,
   rounds INT DEFAULT 1,
   recommended_weight_kg INT DEFAULT 0,
   custom_instruction TEXT,

   UNIQUE(wod_id, exercise_id),
   INDEX idx_wod_exercises_wod (wod_id),
   INDEX idx_wod_exercises_exercise (exercise_id)
   );
6. results (результаты тренировок)
   sql
   CREATE TABLE public.results (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   wod_id UUID REFERENCES public.wods(id) ON DELETE CASCADE,
   user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
   score NUMERIC NOT NULL,
   formatted_score TEXT, -- "15:30" для for_time
   completed_at TIMESTAMP DEFAULT NOW(),
   is_pr BOOLEAN DEFAULT false,
   synced_at TIMESTAMP,

   INDEX idx_results_wod (wod_id),
   INDEX idx_results_user (user_id),
   INDEX idx_results_completed (completed_at),
   INDEX idx_results_score (score) WHERE format = 'for_time'
   );
   Типы связей между таблицами
   Таблица 1	Таблица 2	Тип связи	Пояснение
   profiles	groups	N:1	Много атлетов в одной группе
   groups	profiles	1:N	Один тренер ведёт несколько групп
   wods	groups	N:1	WOD может быть для одной группы или всех
   wods	profiles	N:1	WOD создаётся одним тренером
   wods	exercises	N:N	Через wod_exercises
   results	wods	N:1	Много результатов к одному WOD
   results	profiles	N:1	Много результатов у одного атлета

   Шаблоны разработки (Design Patterns)
1. Repository Pattern (Основной)
Назначение: Абстракция источника данных (Supabase API или Room)
2. MVVM (Model-View-ViewModel)
   Назначение: Разделение UI и бизнес-логики
3. Singleton Pattern
   Назначение: Единый экземпляр клиента Supabase
4. Factory Pattern
   Назначение: Создание ViewModel с зависимостями
5. Observer Pattern
   Назначение: LiveData и Supabase Realtime
6. Strategy Pattern
   Назначение: Разные стратегии подсчёта очков для разных форматов WOD
7. Adapter Pattern
   Назначение: Адаптация данных из Supabase к моделям Android
8. Command Pattern
   Назначение: Инкапсуляция операций с БД для оффлайн-синхронизации