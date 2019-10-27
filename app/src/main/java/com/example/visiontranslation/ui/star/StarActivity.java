package com.example.visiontranslation.ui.star;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;

import androidx.appcompat.widget.DialogTitle;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.visiontranslation.R;
import com.example.visiontranslation.database.DatabaseManager;
import com.example.visiontranslation.database.TextTranslationHistory;
import com.example.visiontranslation.helper.Helper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class StarActivity extends AppCompatActivity {

    private StarHistoryAdapter adapter;
    private List<TextTranslationHistory.TranslationHistory> historyList;
    private SearchView searchView;
    private RecyclerView stars;
    private RecyclerView search;
    private StarHistoryAdapter searchAdapter;
    private int sortType = 3;
    private static final int SORT_BY_A_Z = 0;
    private static final int SORT_BY_Z_A = 1;
    private static final int SORT_BY_TIME_A = 2;
    private static final int SORT_BY_TIME_D = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star);
        init();
    }

    private void init() {
        Toolbar toolbar = findViewById(R.id.star_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        stars = findViewById(R.id.star_history);
        search = findViewById(R.id.star_search_result);

        adapter = new StarHistoryAdapter(getApplicationContext(), this);
        searchAdapter = new StarHistoryAdapter(getApplicationContext(), this);
        stars.setAdapter(adapter);
        search.setAdapter(searchAdapter);
        new Thread(()->{
            getStars();
        }).start();
    }

    private void getStars() {
        historyList = new ArrayList<>();
        DatabaseManager.getTextTranslationHistory().requestLoadHistoryRecord(historyList, new TextTranslationHistory.OnHistoryRecordLoadComplete() {
            @Override
            public void onHistoryRecordLoadComplete(int status, List<TextTranslationHistory.TranslationHistory> data) {
                data.sort(new Comparator<TextTranslationHistory.TranslationHistory>() {
                    @Override
                    public int compare(TextTranslationHistory.TranslationHistory o1, TextTranslationHistory.TranslationHistory o2) {
                        return -(int)(o1.getTime() - o2.getTime());
                    }
                });
                data.removeIf(new Predicate<TextTranslationHistory.TranslationHistory>() {
                    @Override
                    public boolean test(TextTranslationHistory.TranslationHistory translationHistory) {
                        return !translationHistory.isFavorite();
                    }
                });
                adapter.setHistories(data);
            }
        });
    }

    private void sort() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_sort);
        RadioGroup group = dialog.findViewById(R.id.dialog_sort_group);
        Button cancel = dialog.findViewById(R.id.dialog_sort_cancel);
        switch (sortType) {
            case SORT_BY_A_Z: {
                group.check(R.id.dialog_sort_by_a_z);
            } break;

            case SORT_BY_Z_A: {
                group.check(R.id.dialog_sort_by_z_a);
            } break;

            case SORT_BY_TIME_A: {
                group.check(R.id.dialog_sort_by_time_a);
            } break;

            case SORT_BY_TIME_D: {
                group.check(R.id.dialog_sort_by_time_d);
            } break;
        }

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.dialog_sort_by_a_z: {
                        sortType = SORT_BY_A_Z;
                        sortByAZ();
                    } break;

                    case R.id.dialog_sort_by_z_a: {
                        sortType = SORT_BY_Z_A;
                        sortByZA();
                    } break;

                    case R.id.dialog_sort_by_time_a: {
                        sortType = SORT_BY_TIME_A;
                        sortBYTimeA();
                    } break;

                    case R.id.dialog_sort_by_time_d: {
                        sortType = SORT_BY_TIME_D;
                        sortBYTimteD();
                    } break;
                }
                adapter.setHistories(historyList);
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(v->{
            dialog.dismiss();
        });
        dialog.show();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);
        lp.width = width;
        dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    private void sortByAZ() {
        historyList.sort(new Comparator<TextTranslationHistory.TranslationHistory>() {
            @Override
            public int compare(TextTranslationHistory.TranslationHistory o1, TextTranslationHistory.TranslationHistory o2) {
                return -o1.getSource().compareToIgnoreCase(o2.getSource());
            }
        });
    }

    private void sortByZA() {
        historyList.sort(new Comparator<TextTranslationHistory.TranslationHistory>() {
            @Override
            public int compare(TextTranslationHistory.TranslationHistory o1, TextTranslationHistory.TranslationHistory o2) {
                return o1.getSource().compareToIgnoreCase(o2.getSource());
            }
        });
    }

    private void sortBYTimeA() {
        historyList.sort(new Comparator<TextTranslationHistory.TranslationHistory>() {
            @Override
            public int compare(TextTranslationHistory.TranslationHistory o1, TextTranslationHistory.TranslationHistory o2) {
                return (int)(o1.getTime() - o2.getTime());
            }
        });
    }

    private void sortBYTimteD() {
        historyList.sort(new Comparator<TextTranslationHistory.TranslationHistory>() {
            @Override
            public int compare(TextTranslationHistory.TranslationHistory o1, TextTranslationHistory.TranslationHistory o2) {
                return -(int)(o1.getTime() - o2.getTime());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.star_menu, menu);
        searchView = (SearchView)menu.findItem(R.id.star_app_bar_search).getActionView();
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                search.setVisibility(View.GONE);
                stars.setVisibility(View.VISIBLE);
                return true;

            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search.setVisibility(View.VISIBLE);
                stars.setVisibility(View.INVISIBLE);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                search(query, new SearchCallback() {
                    @Override
                    public void onResult(List<TextTranslationHistory.TranslationHistory> data) {
                        searchAdapter.setHistories(data);
                    }
                });
                Helper.hideSoftKeyboard(StarActivity.this);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public void search(String text, SearchCallback callback) {
        new Thread(()->{

        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
            } break;

            case R.id.star_app_bar_search: {

            } break;

            case R.id.star_sort: {
                sort();
            } break;
        }
        return super.onOptionsItemSelected(item);
    }

    public interface SearchCallback {
        public void onResult(List<TextTranslationHistory.TranslationHistory> data);
    }
}
