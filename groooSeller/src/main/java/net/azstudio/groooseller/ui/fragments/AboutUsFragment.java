package net.azstudio.groooseller.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;


import net.azstudio.groooseller.R;
import net.azstudio.groooseller.ui.base.BaseFragment;

import butterknife.BindView;


public class AboutUsFragment extends BaseFragment {

    @BindView(R.id.btn_gotoweb)
    TextView gotoWeb;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_aboutus;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gotoWeb.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("http://www.grooo.cn/");
                intent.setData(content_url);
                startActivity(intent);
            }
        });
    }
}