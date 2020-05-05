package com.xazhuxj.words;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Switch;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.NavigableMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class WordsFragment extends Fragment {

    WordViewModel wordViewModel;
    RecyclerView recyclerView;
    CellAdapter cellNormalAdapter, cellCardAdapter;
    Switch aSwitch;
    FloatingActionButton floatingActionButton;
    private LiveData<List<Word>>filteredWords;
    static final String VIEW_TYPE_SHP = "view_type_shp";
    static final String IS_USING_CARD_VIEW =  "is_using_card_view";

    public WordsFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.clearData:
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setTitle("清空数据");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        wordViewModel.deleteAllWords();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create();
                builder.show();
                break;
            case R.id.switchViewType:
                SharedPreferences shp = requireActivity().getSharedPreferences(VIEW_TYPE_SHP, Context.MODE_PRIVATE);
                boolean viewType = shp.getBoolean(IS_USING_CARD_VIEW, false);
                SharedPreferences.Editor editor = shp.edit();
                if(viewType){
                    recyclerView.setAdapter(cellNormalAdapter);
                    editor.putBoolean(IS_USING_CARD_VIEW, false);
                }else{
                    recyclerView.setAdapter(cellCardAdapter);
                    editor.putBoolean(IS_USING_CARD_VIEW, true);
                }
                editor.apply();

//                if(recyclerView.getAdapter() == cellNormalAdapter){
//                    recyclerView.setAdapter(cellCardAdapter);
//                } else{
//                    recyclerView.setAdapter(cellNormalAdapter);
//                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setMaxWidth(1000);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                Log.d("MyTag", "onQueryTextChange"+newText);
                String pattern = newText.trim();
                filteredWords.removeObservers(requireActivity()); // very important
                filteredWords = wordViewModel.findWordsWithPattern(pattern);
                filteredWords.observe(requireActivity(), new Observer<List<Word>>() {
                    @Override
                    public void onChanged(List<Word> words) {
                        int temp = cellCardAdapter.getItemCount();
                        cellNormalAdapter.setAllWords(words);
                        cellNormalAdapter.notifyDataSetChanged();
                        if(temp != words.size()) {
                            cellCardAdapter.setAllWords(words);
                            cellCardAdapter.notifyDataSetChanged();
                        }
                    }
                });
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_words, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        wordViewModel = new ViewModelProvider(this).get(WordViewModel.class);
        recyclerView = requireView().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        cellNormalAdapter = new CellAdapter(false, wordViewModel);
        cellCardAdapter = new CellAdapter(true, wordViewModel);

        SharedPreferences shp = requireActivity().getSharedPreferences(VIEW_TYPE_SHP, Context.MODE_PRIVATE);
        boolean viewType = shp.getBoolean(IS_USING_CARD_VIEW, false);
        if(viewType )
            recyclerView.setAdapter(cellCardAdapter);
        else
            recyclerView.setAdapter(cellNormalAdapter);

        filteredWords = wordViewModel.getAllWords();
        filteredWords.observe(requireActivity(), new Observer<List<Word>>() {
            @Override
            public void onChanged(List<Word> words) {
                int temp = cellCardAdapter.getItemCount();
                cellNormalAdapter.setAllWords(words);
                cellNormalAdapter.notifyDataSetChanged();
                if(temp != words.size()) {
                    cellCardAdapter.setAllWords(words);
                    cellCardAdapter.notifyDataSetChanged();
                }
            }
        });

        floatingActionButton = requireActivity().findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_wordsFragment_to_addFragment);
            }
        });

    }
}
