package ru.am.conduct_rules.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import ru.am.conduct_rules.R;
import ru.am.conduct_rules.RuleInfo;

public class SwipeDeckAdapter extends BaseAdapter {

    private List<RuleInfo> data;
    private Context context;

    public SwipeDeckAdapter(List<RuleInfo> data, Context context) {
        this.data = data;
        this.context = context;
    }

    public RuleInfo getRuleInfo(int position) {
        if (position < data.size())
            return data.get(position);
        return null;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        if (v == null) {
            v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.rule_card, parent, false);
        }
        RuleInfo info = data.get(position);
        String code = "Правило № " + info.code;
        ((TextView) v.findViewById(R.id.text_code)).setText(code);
        ((TextView) v.findViewById(R.id.text_rule)).setText(info.name);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String item = (String) getItem(position);
                Log.i("StackActivity", item);
            }
        });

        return v;
    }
}
