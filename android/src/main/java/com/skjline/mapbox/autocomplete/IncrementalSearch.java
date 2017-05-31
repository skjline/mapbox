package com.skjline.mapbox.autocomplete;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.skjline.mapbox.R;
import com.skjline.mapbox.service.MapBoxService;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class IncrementalSearch extends LinearLayout implements SearchPresenter.Presenter {
    private EditText etSearchText;
    private ImageButton btnControl;
    private ListView lvResults;

    private ArrayAdapter<String> adapter;
    private List<Address> searches;
    private SearchPresenter.View searchView;

    public IncrementalSearch(Context context) {
        super(context);
        initialize();
    }

    public IncrementalSearch(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public IncrementalSearch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    // Requires API 21+
    @SuppressLint("NewApi")
    public IncrementalSearch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        inflate(getContext(), R.layout.incremental_search, this);
        searches = new ArrayList<>();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        etSearchText = (EditText) findViewById(R.id.et_search_text);
        lvResults = (ListView) findViewById(R.id.lv_search_result);
        btnControl = (ImageButton) findViewById(R.id.btn_search_control);
        btnControl.setOnClickListener(view -> toggleVisibility());

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        lvResults.setAdapter(adapter);
        lvResults.setOnItemClickListener((adapterView, view, position, id) -> {
            Address address = searches.get(position);
            searchView.updatePosition(true, new LatLng(address.getLatitude(), address.getLongitude()));
            toggleVisibility();
        });
    }

    @Override
    public void setupSearch(final MapBoxService search, final SearchPresenter.View view) {
        searchView = view;
        etSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @SuppressLint("NewApi")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (TextUtils.isEmpty(charSequence)) {
                    return;
                }

                search.getAutocomplete(charSequence.toString())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(addresses -> {
                            int visibility = addresses.size() > 0 ? VISIBLE : GONE;
                            lvResults.setVisibility(visibility);
                            searches.clear();

                            if (visibility == GONE) {
                                return;
                            }

                            searches = addresses;
                            adapter.clear();

                            // Stream requires API 21+
                            addresses.stream()
                                    .forEach(address -> adapter.add(address.getFeatureName()));
                            adapter.notifyDataSetChanged();
                        });
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void toggleVisibility() {
        if (etSearchText.getVisibility() == VISIBLE) {
            etSearchText.setVisibility(GONE);
            lvResults.setVisibility(GONE);
            btnControl.setImageDrawable(
                    ContextCompat.getDrawable(getContext(), R.drawable.ic_search_black_24dp)
            );

            etSearchText.getText().clear();
            hideKeyboard();
        } else {
            etSearchText.clearFocus();
            etSearchText.setVisibility(VISIBLE);
            btnControl.setImageDrawable(
                    ContextCompat.getDrawable(getContext(), R.drawable.ic_clear_black_24dp)
            );
        }
    }

    private void hideKeyboard() {
        InputMethodManager input = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (!input.isActive()) {
            return;
        }

        input.hideSoftInputFromWindow(getApplicationWindowToken(), 0);
    }
}
