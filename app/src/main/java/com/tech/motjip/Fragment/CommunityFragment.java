package com.tech.motjip.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tech.motjip.Adapter.CommunityPostAdapter;
import com.tech.motjip.CommunityDetailActivity;
import com.tech.motjip.Controller.CommunityController;
import com.tech.motjip.Model.CommunityPost;
import com.tech.motjip.R;
import com.tech.motjip.WriteActivity;

import java.util.ArrayList;
import java.util.List;

public class CommunityFragment extends Fragment {

    private Button btnWrite;

    private ImageView btnSearch;
    private ImageView btnKorean;
    private ImageView btnWestern;
    private ImageView btnJapanese;
    private ImageView btnChinese;
    private ImageView btnStreetFood;
    private ImageView btnCafe;

    private EditText etSearchTitle;

    private TextView tvLocation;
    private TextView tvShowAll;

    private RecyclerView recyclerViewPosts;

    private CommunityPostAdapter adapter;

    private CommunityController communityController;

    private ActivityResultLauncher<Intent> writeActivityLauncher;

    private ActivityResultLauncher<Intent> detailActivityLauncher;

    private String selectedTitle = null;
    private String selectedTag = null;
    private String selectedRegion = null;
    private String selectedSort = "new";

    private static final String PREF_NAME = "AppPrefs";

    private static final String ACCESS_TOKEN =
            "ACCESS_TOKEN";

    public CommunityFragment() {
    }

    @Override
    public void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        writeActivityLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {

                            if (result.getResultCode()
                                    == Activity.RESULT_OK) {

                                resetFilterToDefault();

                                if (recyclerViewPosts != null) {
                                    recyclerViewPosts.scrollToPosition(0);
                                }

                                resetPagingAndLoad();
                            }
                        }
                );

        detailActivityLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {

                            resetPagingAndLoad();
                        }
                );
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(
                R.layout.fragment_community,
                container,
                false
        );

        communityController =
                new CommunityController(requireContext());

        btnWrite = view.findViewById(R.id.btnWrite);

        btnSearch = view.findViewById(R.id.btnSearch);

        etSearchTitle =
                view.findViewById(R.id.etSearchTitle);

        tvLocation =
                view.findViewById(R.id.tvLocation);

        tvShowAll =
                view.findViewById(R.id.tvShowAll);

        btnKorean =
                view.findViewById(R.id.btnKorean);

        btnWestern =
                view.findViewById(R.id.btnWestern);

        btnJapanese =
                view.findViewById(R.id.btnJapanese);

        btnChinese =
                view.findViewById(R.id.btnChinese);

        btnStreetFood =
                view.findViewById(R.id.btnStreetFood);

        btnCafe =
                view.findViewById(R.id.btnCafe);

        recyclerViewPosts =
                view.findViewById(R.id.recyclerViewPosts);

        adapter =
                new CommunityPostAdapter(
                        requireContext(),

                        post -> {

                            Intent intent =
                                    new Intent(
                                            requireContext(),
                                            CommunityDetailActivity.class
                                    );

                            intent.putExtra(
                                    "communityPost",
                                    post
                            );

                            detailActivityLauncher.launch(intent);
                        },

                        new CommunityPostAdapter.OnPostMenuClickListener() {

                            @Override
                            public void onEditClick(
                                    CommunityPost post
                            ) {

                                Intent intent =
                                        new Intent(
                                                requireContext(),
                                                WriteActivity.class
                                        );

                                intent.putExtra(
                                        "isEditMode",
                                        true
                                );

                                intent.putExtra(
                                        "communityPost",
                                        post
                                );

                                writeActivityLauncher.launch(intent);
                            }

                            @Override
                            public void onDeleteClick(
                                    CommunityPost post,
                                    int position
                            ) {

                                communityController.deleteCommunityPost(
                                        post.getComId(),
                                        new CommunityController.DeleteCommunityCallback() {

                                            @Override
                                            public void onSuccess() {

                                                adapter.removePost(position);

                                                Toast.makeText(
                                                        requireContext(),
                                                        "게시글이 삭제되었습니다.",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }

                                            @Override
                                            public void onError(
                                                    String message
                                            ) {

                                                Toast.makeText(
                                                        requireContext(),
                                                        message,
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                        }
                                );
                            }
                        }
                );

        recyclerViewPosts.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        recyclerViewPosts.setAdapter(adapter);

        setupRecyclerViewPagination();

        setupWriteButton();

        setupSearch();

        setupRegionMenu();

        setupSortMenu();

        setupCategoryButtons();

        resetFilterToDefault();

        resetPagingAndLoad();

        return view;
    }

    private void resetFilterToDefault() {

        selectedTitle = null;

        selectedTag = null;

        selectedRegion = null;

        selectedSort = "new";

        if (etSearchTitle != null) {
            etSearchTitle.setText("");
        }

        if (tvShowAll != null) {
            tvShowAll.setText("전체보기 〉");
        }

        if (tvLocation != null) {
            tvLocation.setText("지역 전체 〉");
        }
    }

    private void setupWriteButton() {

        btnWrite.setOnClickListener(v -> {

            SharedPreferences prefs =
                    requireActivity().getSharedPreferences(
                            PREF_NAME,
                            Context.MODE_PRIVATE
                    );

            String accessToken =
                    prefs.getString(
                            ACCESS_TOKEN,
                            null
                    );

            if (accessToken == null
                    || accessToken.isEmpty()) {

                Toast.makeText(
                        getContext(),
                        "로그인 후 이용해주세요",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Intent intent =
                    new Intent(
                            getActivity(),
                            WriteActivity.class
                    );

            writeActivityLauncher.launch(intent);
        });
    }

    private void setupSearch() {

        btnSearch.setOnClickListener(v -> {

            String keyword =
                    etSearchTitle
                            .getText()
                            .toString()
                            .trim();

            selectedTitle =
                    keyword.isEmpty()
                            ? null
                            : keyword;

            resetPagingAndLoad();
        });

        etSearchTitle.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId
                            == EditorInfo.IME_ACTION_SEARCH) {

                        String keyword =
                                etSearchTitle
                                        .getText()
                                        .toString()
                                        .trim();

                        selectedTitle =
                                keyword.isEmpty()
                                        ? null
                                        : keyword;

                        resetPagingAndLoad();

                        return true;
                    }

                    return false;
                }
        );
    }

    private void setupRegionMenu() {

        tvLocation.setOnClickListener(v -> {

            PopupMenu popupMenu =
                    new PopupMenu(
                            requireContext(),
                            tvLocation
                    );

            popupMenu.getMenu().add("지역 전체");
            popupMenu.getMenu().add("서울");
            popupMenu.getMenu().add("부산");
            popupMenu.getMenu().add("대구");
            popupMenu.getMenu().add("인천");
            popupMenu.getMenu().add("광주");
            popupMenu.getMenu().add("대전");
            popupMenu.getMenu().add("울산");
            popupMenu.getMenu().add("세종");
            popupMenu.getMenu().add("경기");
            popupMenu.getMenu().add("강원");
            popupMenu.getMenu().add("충북");
            popupMenu.getMenu().add("충남");
            popupMenu.getMenu().add("전북");
            popupMenu.getMenu().add("전남");
            popupMenu.getMenu().add("경북");
            popupMenu.getMenu().add("경남");
            popupMenu.getMenu().add("제주");

            popupMenu.setOnMenuItemClickListener(item -> {

                String selected =
                        item.getTitle().toString();

                if (selected.equals("지역 전체")) {

                    selectedRegion = null;

                    tvLocation.setText("지역 전체 〉");

                } else {

                    selectedRegion = selected;

                    tvLocation.setText(
                            selected + " 〉"
                    );
                }

                resetPagingAndLoad();

                return true;
            });

            popupMenu.show();
        });
    }

    private void setupSortMenu() {

        tvShowAll.setOnClickListener(v -> {

            PopupMenu popupMenu =
                    new PopupMenu(
                            requireContext(),
                            tvShowAll
                    );

            popupMenu.getMenu().add("전체보기");
            popupMenu.getMenu().add("최신순");
            popupMenu.getMenu().add("오래된순");

            popupMenu.setOnMenuItemClickListener(item -> {

                String selected =
                        item.getTitle().toString();

                if (selected.equals("전체보기")) {

                    selectedTitle = null;

                    selectedTag = null;

                    selectedSort = "new";

                    etSearchTitle.setText("");

                    tvShowAll.setText("전체보기 〉");

                } else if (selected.equals("오래된순")) {

                    selectedSort = "old";

                    tvShowAll.setText("오래된순 〉");

                } else {

                    selectedSort = "new";

                    tvShowAll.setText("최신순 〉");
                }

                resetPagingAndLoad();

                return true;
            });

            popupMenu.show();
        });
    }

    private void setupCategoryButtons() {

        btnKorean.setOnClickListener(v -> {
            selectedTag = "한식";
            resetPagingAndLoad();
        });

        btnWestern.setOnClickListener(v -> {
            selectedTag = "양식";
            resetPagingAndLoad();
        });

        btnJapanese.setOnClickListener(v -> {
            selectedTag = "일식";
            resetPagingAndLoad();
        });

        btnChinese.setOnClickListener(v -> {
            selectedTag = "중식";
            resetPagingAndLoad();
        });

        btnStreetFood.setOnClickListener(v -> {
            selectedTag = "분식";
            resetPagingAndLoad();
        });

        btnCafe.setOnClickListener(v -> {
            selectedTag = "카페";
            resetPagingAndLoad();
        });
    }

    private void resetPagingAndLoad() {

        communityController.loadFirstPage(
                selectedTitle,
                selectedTag,
                selectedRegion,
                selectedSort,
                new CommunityController.CommunityCallback() {

                    @Override
                    public void onSuccess(
                            List<CommunityPost> posts,
                            boolean isLastPage
                    ) {

                        adapter.clearPosts();

                        List<CommunityPost> activePosts =
                                new ArrayList<>();

                        for (CommunityPost post : posts) {

                            if (!post.isClosed()) {

                                activePosts.add(post);
                            }
                        }

                        adapter.addPosts(activePosts);
                    }

                    @Override
                    public void onEmpty() {

                        adapter.clearPosts();

                        Toast.makeText(
                                getContext(),
                                "조건에 맞는 게시글이 없습니다.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        Toast.makeText(
                                getContext(),
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void setupRecyclerViewPagination() {

        recyclerViewPosts.addOnScrollListener(
                new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrolled(
                            @NonNull RecyclerView recyclerView,
                            int dx,
                            int dy
                    ) {
                        super.onScrolled(
                                recyclerView,
                                dx,
                                dy
                        );

                        LinearLayoutManager layoutManager =
                                (LinearLayoutManager)
                                        recyclerView.getLayoutManager();

                        if (layoutManager == null) {
                            return;
                        }

                        int lastVisiblePosition =
                                layoutManager.findLastVisibleItemPosition();

                        int totalItemCount =
                                layoutManager.getItemCount();

                        if (lastVisiblePosition
                                >= totalItemCount - 3) {

                            communityController.loadNextPage(
                                    selectedTitle,
                                    selectedTag,
                                    selectedRegion,
                                    selectedSort,
                                    new CommunityController.CommunityCallback() {

                                        @Override
                                        public void onSuccess(
                                                List<CommunityPost> posts,
                                                boolean isLastPage
                                        ) {

                                            List<CommunityPost> activePosts =
                                                    new ArrayList<>();

                                            for (CommunityPost post : posts) {

                                                if (!post.isClosed()) {

                                                    activePosts.add(post);
                                                }
                                            }

                                            adapter.addPosts(activePosts);
                                        }

                                        @Override
                                        public void onEmpty() {
                                        }

                                        @Override
                                        public void onError(
                                                String message
                                        ) {

                                            Toast.makeText(
                                                    getContext(),
                                                    message,
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    }
                            );
                        }
                    }
                }
        );
    }
}