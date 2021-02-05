package com.superb20.nima;

import android.support.v4.app.Fragment;

import com.superb20.nima.Common.SingleFragmentActivity;

/**
 * Created by Superb20 on 2019-02-14.
 */

public class MainActivity extends SingleFragmentActivity {
    @Override
    public Fragment createFragment() {
        return new MainFragment().newInstance();
    }
}
