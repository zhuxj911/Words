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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.Switch;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

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

    private List<Word> allWords;
    private  boolean undoAction = false;
    private DividerItemDecoration dividerItemDecoration;

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
                boolean isUsingCardView = shp.getBoolean(IS_USING_CARD_VIEW, false);
                SharedPreferences.Editor editor = shp.edit();
                if(isUsingCardView){
                    recyclerView.setAdapter(cellNormalAdapter);
                    recyclerView.addItemDecoration(dividerItemDecoration);
                    editor.putBoolean(IS_USING_CARD_VIEW, false);
                }else{
                    recyclerView.setAdapter(cellCardAdapter);
                    recyclerView.removeItemDecoration(dividerItemDecoration);
                    editor.putBoolean(IS_USING_CARD_VIEW, true);
                }
                editor.apply();
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
                filteredWords.removeObservers(getViewLifecycleOwner()); // very important
                filteredWords = wordViewModel.findWordsWithPattern(pattern);
                filteredWords.observe(getViewLifecycleOwner(), words -> {
                    int temp = cellCardAdapter.getItemCount();
                    allWords = words;
                    if(temp != words.size()) {
                        cellNormalAdapter.submitList(words);
                        cellCardAdapter.submitList(words);
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
    public void onResume() {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        super.onResume();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        wordViewModel = new ViewModelProvider(this).get(WordViewModel.class);
        recyclerView = requireView().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        cellNormalAdapter = new CellAdapter(false, wordViewModel);
        cellCardAdapter = new CellAdapter(true, wordViewModel);
        recyclerView.setItemAnimator(new DefaultItemAnimator(){
            @Override
            public void onAnimationFinished(@NonNull RecyclerView.ViewHolder viewHolder) {
                super.onAnimationFinished(viewHolder);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if(linearLayoutManager != null){
                    int firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
                    int lastPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                    for(int i=firstPosition; i<=lastPosition; i++){
                        CellAdapter.CellViewHolder holder = (CellAdapter.CellViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
                        if(holder != null){
                            holder.textViewNumber.setText(String.valueOf(i+1));
                        }
                    }
                }
            }
        });

        SharedPreferences shp = requireActivity().getSharedPreferences(VIEW_TYPE_SHP, Context.MODE_PRIVATE);
        boolean viewType = shp.getBoolean(IS_USING_CARD_VIEW, false);
        dividerItemDecoration = new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL);
        if(viewType )
            recyclerView.setAdapter(cellCardAdapter);
        else {
            recyclerView.setAdapter(cellNormalAdapter);
            recyclerView.addItemDecoration(dividerItemDecoration);
        }

        filteredWords = wordViewModel.getAllWords();
        filteredWords.observe(getViewLifecycleOwner(), words -> {
            int temp = cellCardAdapter.getItemCount();
            allWords = words;
            if(temp != words.size()) {
                if(temp < words.size() && !undoAction){
                    recyclerView.smoothScrollBy(0, -200);
                }
                undoAction = false;
                cellNormalAdapter.submitList(words);
                cellCardAdapter.submitList(words);
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.UP,
                ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
//                Word wordFrom = allWords.get(viewHolder.getAdapterPosition());
//                Word wordTo = allWords.get(target.getAdapterPosition());
//                int iTemp = wordFrom.getId();
//                wordFrom.setId(wordTo.getId());
//                wordTo.setId(iTemp);
//                wordViewModel.updateWords(wordFrom, wordTo);
//                cellNormalAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
//                cellCardAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final Word wordToDelete = allWords.get(viewHolder.getAdapterPosition());
                wordViewModel.deleteWords(wordToDelete);
                Snackbar.make(requireActivity().findViewById(R.id.wordsFragmentView), "删除了一个单词", Snackbar.LENGTH_SHORT)
                        .setAction("撤销", new View.OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                undoAction = true;
                                wordViewModel.insertWords(wordToDelete);
                            }
                        })
                        .show();
            }
        }).attachToRecyclerView(recyclerView);

        floatingActionButton = requireActivity().findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_wordsFragment_to_addFragment);
        });

    }
}
