package ru.am.conduct_rules;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    String sqlRules;

    public DBHelper(Context context) {
        super(context, Consts.DATABASE_NAME, null, Consts.DB_VERSION);
        Log.d("DBName", Consts.DATABASE_NAME);
        sqlRules = context.getString(R.string.sql_rules);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE user ("
                + "_id INTEGER PRIMARY KEY,"
                + "name TEXT,"
                + "age INTEGER,"
                + "meditation TEXT,"
                + "gender INTEGER,"
                + "start INTEGER DEFAULT 0,"
                + "mode INTEGER DEFAULT 0,"
                + "reminder INTEGER DEFAULT 1,"
                + "reminder_time INTEGER DEFAULT 1200,"
                + "reg_date INTEGER,"
                + "language INTEGER);");

        db.execSQL("INSERT INTO user (_id, name, gender, language) VALUES (1, 'Садхака', NULL, 0)");

        db.execSQL("CREATE TABLE rule ("
                + "_id INTEGER PRIMARY KEY,"
                + "level INTEGER,"
                + "code TEXT,"
                + "name TEXT,"
                + "description TEXT,"           // описание
                + "benefits TEXT,"              // какую пользу дает
                + "instructions TEXT,"          // инструкция
                + "links TEXT,"
                + "point INTEGER,"
                + "vidible INTEGER DEFAULT 1,"  // оценка правила. 0 - скрыто
                + "available INTEGER DEFAULT 0,"
                + "checked INTEGER DEFAULT 0,"
                + "estimate INTEGER DEFAULT 0,"  // оценка правила. 0 - не оценено, 1 - плохо, 2 - хорошо, 3 - идеально
                + "done INTEGER DEFAULT 0);"); // 0 - не выполнено, 1 - выполнено частично, 2 - выполнено полностью

        loadRules(db);

        db.execSQL("UPDATE rule SET available = 1 WHERE level = 1");

        db.execSQL("CREATE TABLE practice ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "rule_id INTEGER,"
                + "date INTEGER,"
                + "result INTEGER,"
                + "done INTEGER DEFAULT 0)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }
    private void loadRules(SQLiteDatabase db) {

        db.execSQL("INSERT INTO rule (_id, level, code, name, description, benefits, instructions, links) " +
                "VALUES (1, 1, '1.1', 'Использование воды после туалета', 'Омывание органов после мочеиспускания и дефекации', " +
                "'   - Сохранение здоровья репродуктивной системы.\n" +
                "    - Поддержание чистоты. Сходил в туалет - загрязнился, нужно очиститься.\n" +
                "    - Охлаждение для успокоения нервной системы и чакр\n" +
                "    - Охлаждение для сокращения мышц и соответственно эвакуации остатков урины\n" +
                "', '    - После мочеиспускания или дефекации омыть органы холодной водой, используя левую руку.\n" +
                "    - Чтобы всегда была вода для омывания, стоит носить с собой бутылочку с водой. Такая ёмкость с водой называется шауча манджуса.\n" +
                "', null);");

        db.execSQL("INSERT INTO rule (_id, level, code, name, description, benefits, instructions, links) " +
                "VALUES (2, 1, '1.2', 'Убирать крайнюю плоть назад', 'Оттягивание крайней плоти для мужчин', '    - Улучшает гигиену полового органа\n" +
                "    - Уменьшает чувствительность, что способствует снижению стимуляции полового органа\n" +
                "', '    - Оттягивать крайнюю плоть. Если никак не отодвигается, то необходимо хирургическое вмешательство, для того чтобы расширить проход и кожа сдвигалась.\n" +
                "    - Чтобы крайняя плоть не сдвигалась обратно, и половой орган был зафиксирован, следует носить ланготу.\n" +
                "', null);");

        db.execSQL("INSERT INTO rule (_id, level, code, name, description, benefits, instructions, links) " +
                "VALUES (3, 1, '1.3', 'Не брить волосы в паху', 'Не сбривать волосы в паху', '    - Волосы в паху осуществляют функцию терморегуляции организма — защищают от перегрева и переохлаждения. Это важно для лимфатической системы, так как в паху и подмышках находятся лимфатические узлы.\n" +
                "    - Если не удалять эти волосы, идёт меньше расход энергии на физическом уровне, и её сохраняется больше для ментального уровня\n" +
                "', '    - Оставлять волосы в паху небритыми. Тщательно вытирать после мытья.', null);");

        db.execSQL("INSERT INTO rule (_id, level, code, name, description, benefits, instructions, links) " +
                "VALUES (4, 1, '1.4', 'Менять нижнее бельё ежедневно', 'Во избежание негативного эффекта на тело и разум, и для поддержания гигиены необходимо ежедневно менять нижнее бельё.', '     - Поддержание гигиены тела.\n" +
                "     - Поддержание чистоты разума', '    - Каждый день надевать чистое нижнее бельё после мытья', null);");

        db.execSQL("INSERT INTO rule (_id, level, code, name, description, benefits, instructions, links) " +
                "VALUES (5, 1, '1.5', 'Носить ланготы', 'Использование мужчинами лангот вместо трусов', '    - Половые органы находятся в зафиксированном положении. Меньше стимуляции.\n" +
                "    - Улучшение здоровья репродуктивной системы\n" +
                "    - Улучшение производства мужских гормонов.\n" +
                "    - Увеличение ментальной силы. Разум меньше отвлекается.\n" +
                "    - Меньше стимуляции во время выполнения асан', '    \n" +
                "    - Постоянно носить ланготы вместо другого нижнего белья. Каждый день надевать чистые ланготы.', null);\n");
        db.execSQL("INSERT INTO rule (_id, level, code, name, description, benefits, instructions, links) " +
                "VALUES (6, 1, '1.6', 'Полуванна перед садханой', 'Выполнение полуванны (полуомовения) перед медитацией и асанами', '    - Освежает разум, взбадривает и стимулирует мозг\n" +
                "    - Успокаивает дыхание и сердцебиение, что помогает глубже концентрироваться в медитации\n" +
                "    - Закаливает организм\n" +
                "    - Улучшает пищеварение, кровоток к внутренним органам\n" +
                "    - Помогает не переедать\n" +
                "    - Охлаждение нижних центров помогает контролировать низкие инстинкты\n" +
                "    - Успокаивает нервную систему перед сном. Сон становится глубже и спокойнее\n" +
                "    - Вода помогает обнулить энергетику, тем самым успокоить разум\n" +
                "    - Процедура брызгания водой в глаза снимает напряжение и усталость с глаз, облегчает головную боль.', '    - Смочить холодной водой пупок, затем область ниже пупка и гениталии. После ополоснуть  руки вниз от локтей и ноги вниз от колен, стопы. Затем набрать в рот воды и плескать воду в глаза 12 раз, и выплюнуть воду. Далее охладить лицо, уши, область за ушами и шею.\n" +
                "    - Полуванну рекомендуется выполнять холодной водой — чем холоднее, тем лучше. Температуру воды снижайте постепенно, как при всех техниках закаливания.\n" +
                "\n" +
                "    Меры предосторожности\n" +
                "\n" +
                "    - температуру воды снижайте постепенно, как при всех техниках закаливания;носителям контактных линз лучше проводить эту практику до того, как вы утром надели линзы и после того, как вечером вы их сняли.\n" +
                "\n" +
                "', null);");


    }

}

