package ru.am.conduct_rules.ui.list_rules;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import ru.am.conduct_rules.DBHelper;
import ru.am.conduct_rules.DataModule;
import ru.am.conduct_rules.R;
import ru.am.conduct_rules.RuleInfo;
import ru.am.conduct_rules.databinding.FragmentListRulesBinding;

import java.util.Date;

public class ListRulesFragment extends Fragment {

    private FragmentListRulesBinding binding;

    private LinearLayout mLinerLayoutU1;
    private LinearLayout mLinerLayoutU2;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentListRulesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mLinerLayoutU1 = root.findViewById(R.id.listRules1);
        mLinerLayoutU2 = root.findViewById(R.id.listRules2);

        loadListRules();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateRule(RuleInfo rule) {
        ContentValues cv = new ContentValues();
        cv.put("checked", rule.checked ? 1 : 0);
        DataModule.dbWriter.update("rule", cv, "_id = ?", new String[]{String.valueOf(rule.id)});
        DataModule.dbWriter.delete("practice", "rule_id = ?", new String[]{String.valueOf(rule.id)});
        if (rule.checked) {
            int startDate = (int) (new Date().getTime() / (1000 * 86400));
            int endDate = startDate + 20;
            for (int i = startDate; i <= endDate; i++) {
                cv.clear();
                cv.put("rule_id", rule.id);
                cv.put("date", i);
                DataModule.dbWriter.insert("practice", null, cv);
            }
        }
    }

    private void deleteRule(RuleInfo rule, Button buttonAdd) {

        final Boolean[] result = {false};

        AlertDialog.Builder ad;
        String title = "Подтверждение удаления";
        String message = "Удалить правило из практики?\n\"" + rule.name + "\"";
        String buttonYesString = "Да";
        String buttonNoString = "Нет";

        ad = new AlertDialog.Builder(getContext());
        ad.setTitle(title);
        ad.setMessage(message);

        ad.setPositiveButton(buttonYesString, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                buttonAdd.setBackgroundResource(R.drawable.ic_add);
                rule.checked = false;
                updateRule(rule);
                Toast toast = Toast.makeText(getContext(), rule.name + "\nУдалено из практики!", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        ad.setNegativeButton(buttonNoString, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

            }
        });
        ad.setCancelable(false);
        ad.show();
    }

    View.OnClickListener buttonAddRuleClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button buttonAdd = (Button) v;
            RuleInfo rule = (RuleInfo) buttonAdd.getTag();
            if (rule.checked) {
                deleteRule(rule, buttonAdd);
            } else {
                if (isCanRule()) {
                    buttonAdd.setBackgroundResource(R.drawable.ic_remove);
                    rule.checked = true;
                    updateRule(rule);
                    Toast toast = Toast.makeText(getContext(), rule.name + "\nДобавлено в практику!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }
    };

    private boolean isCanRule() {

        int count = getCountPractices();
        if (count >= 5) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Выбор правил для практики")
                    .setMessage("В практику уже добавлено 5 правил!)")
                    .setIcon(R.drawable.ic_do_not_touch)
                    .setCancelable(false)
                    .setNegativeButton("ОК",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();

            return false;
        }
        return true;
    }

    private int getCountPractices() {

        int date = (int) (new Date().getTime() / (1000 * 86400));
        String strDate = String.valueOf(date);

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT COUNT(r._id)" +
                " FROM rule r JOIN practice p WHERE r._id = p.rule_id AND p.done = 0 AND p.date = " +
                strDate, null);
        if (cursor.moveToFirst())
            return cursor.getInt(0);
        return 0;
    }


    private void loadListRules() {

        Context context = getContext();
        if (context == null)
            return;

        int height = DataModule.convertDpToPixel(130, context);
        int sizeButtonAdd = DataModule.convertDpToPixel(36, context);
        int sizeButtonNot = DataModule.convertDpToPixel(30, context);
        int paddingDP = DataModule.convertDpToPixel(4, context);

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT _id, code, name, level, checked, available" +
                " FROM rule WHERE done = 0 ORDER BY _id", null);
        if ((cursor != null)) {
            while (cursor.moveToNext()) {

                RuleInfo rule = new RuleInfo();
                rule.id = cursor.getInt(0);
                rule.name = cursor.getString(2);
                rule.level = cursor.getInt(3);
                rule.checked = cursor.getInt(4) == 1;
                rule.available = cursor.getInt(5) == 1;

                LinearLayout wrapper = new LinearLayout(context);
                wrapper.setTag(rule.id);
                wrapper.setOrientation(LinearLayout.VERTICAL);
                wrapper.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        height));

                LinearLayout wrapperButton = new LinearLayout(context);
                View emptyView = new View(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 0);
                params.weight = 1;
                emptyView.setLayoutParams(params);
                wrapperButton.addView(emptyView);
                wrapperButton.setOrientation(LinearLayout.HORIZONTAL);
                wrapperButton.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        sizeButtonAdd));

                Button buttonAdd = new Button(context);
                if (rule.level == 1) {
                    buttonAdd.setWidth(sizeButtonAdd);
                    buttonAdd.setHeight(sizeButtonAdd);
                    buttonAdd.setLayoutParams(new FrameLayout.LayoutParams(sizeButtonAdd, sizeButtonAdd));
                } else {
                    buttonAdd.setWidth(sizeButtonNot);
                    buttonAdd.setHeight(sizeButtonNot);
                    buttonAdd.setLayoutParams(new FrameLayout.LayoutParams(sizeButtonNot, sizeButtonNot));
                }
                buttonAdd.setBackgroundResource(rule.available ?
                        (rule.checked ? R.drawable.ic_remove : R.drawable.ic_add) : R.drawable.ic_not);
                if (rule.available) {
                    buttonAdd.setOnClickListener(buttonAddRuleClickListener);
                    buttonAdd.setTag(rule);
                }

                wrapperButton.addView(buttonAdd);

                wrapper.addView(wrapperButton);

                TextView textViewRule = new TextView(context);
                textViewRule.setText(rule.name);
                textViewRule.setHeight(height);
                textViewRule.setGravity(Gravity.CENTER_HORIZONTAL);
                textViewRule.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                textViewRule.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                textViewRule.setPadding(paddingDP, 0, paddingDP, 0);
                wrapper.addView(textViewRule);

                if (rule.level == 1) {
                    wrapper.setBackground(context.getDrawable(R.drawable.cell_shape_orange));
                    mLinerLayoutU1.addView(wrapper);
                } else {
                    wrapper.setBackground(context.getDrawable(R.drawable.cell_shape_gray));
                    mLinerLayoutU2.addView(wrapper);
                }

            }
        }
        cursor.close();

    }
}