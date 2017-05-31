package com.skjline.mapbox.util;

import android.view.View;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class RxListener {
    public static Observable<View> getOnClickObservable(final View view) {
        return Observable.create(new ObservableOnSubscribe<View>() {
            @Override
            public void subscribe(final ObservableEmitter<View> emitter) throws Exception {
                view.setOnClickListener(vw -> {
                    if (emitter.isDisposed()) {
                        vw.setOnClickListener(null);
                        return;
                    }

                    emitter.onNext(vw);
                });
            }
        });
    }

    public static Observable<View> getOnClickViewObservable(final View view) {
        return Observable.create(emitter -> view.setOnClickListener(vw -> {
                    if (emitter.isDisposed()) {
                        vw.setOnClickListener(null);
                        return;
                    }

                    emitter.onNext(vw);
                })
        );
    }
}

