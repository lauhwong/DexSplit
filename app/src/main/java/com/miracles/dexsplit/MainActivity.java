package com.miracles.dexsplit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.miracles.dexsplit.mp.A;
import com.miracles.dexsplit.mp.B;
import com.miracles.dexsplit.mp.C;

import rx.plugins.RxJavaSchedulersHook;

public class MainActivity extends AppCompatActivity {
    private B b = new B();
    private C c = new C();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RxJavaSchedulersHook.createComputationScheduler();
        b.method_0();
    }
}
