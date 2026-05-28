package com.tech.motjip.manager;

import android.content.Context;
import android.util.Log;
import android.widget.Button;

import com.tech.motjip.R;

import java.util.ArrayList;
import java.util.List;

public class ReviewTagManager {

    private static final String TAG =
            "ReviewTagManagerDebug";

    private final Context context;

    private final List<String> selectedTags =
            new ArrayList<>();

    public ReviewTagManager(
            Context context
    ) {

        this.context =
                context;
    }

    public void bindTagButton(
            Button button,
            String tagName
    ) {

        if (button == null) {

            Log.e(
                    TAG,
                    "bindTagButton button null, tagName = "
                            + tagName
            );

            return;
        }

        clearButtonTint(
                button
        );

        button.setBackgroundResource(
                R.drawable.review_tag_unselected
        );

        button.setTextColor(
                context.getColor(
                        android.R.color.black
                )
        );

        button.setText(
                tagName
        );

        button.setOnClickListener(v -> {

            toggleTag(
                    button,
                    tagName
            );
        });
    }

    private void toggleTag(
            Button button,
            String tagName
    ) {

        Log.d(
                TAG,
                "태그 클릭 = "
                        + tagName
        );

        if (selectedTags.contains(tagName)) {

            selectedTags.remove(
                    tagName
            );

            clearButtonTint(
                    button
            );

            button.setBackgroundResource(
                    R.drawable.review_tag_unselected
            );

            button.setTextColor(
                    context.getColor(
                            android.R.color.black
                    )
            );

            button.setText(
                    tagName
            );

            Log.d(
                    TAG,
                    "태그 해제 = "
                            + tagName
            );

        } else {

            selectedTags.add(
                    tagName
            );

            clearButtonTint(
                    button
            );

            button.setBackgroundResource(
                    R.drawable.review_tag_selected
            );

            button.setTextColor(
                    context.getColor(
                            android.R.color.white
                    )
            );

            button.setText(
                    "✓ " + tagName
            );

            Log.d(
                    TAG,
                    "태그 선택 = "
                            + tagName
            );
        }

        Log.d(
                TAG,
                "현재 선택 태그 = "
                        + selectedTags
        );
    }

    /*
     * 태그 강제 선택
     * 수정 모드에서 기존 태그 표시용
     */
    public void selectTag(
            Button button,
            String tagName
    ) {

        if (button == null
                || tagName == null
                || tagName.trim().isEmpty()) {

            Log.e(
                    TAG,
                    "selectTag 실패 button/tagName null"
            );

            return;
        }

        if (!selectedTags.contains(tagName)) {

            selectedTags.add(
                    tagName
            );
        }

        clearButtonTint(
                button
        );

        button.setBackgroundResource(
                R.drawable.review_tag_selected
        );

        button.setTextColor(
                context.getColor(
                        android.R.color.white
                )
        );

        button.setText(
                "✓ " + tagName
        );

        Log.d(
                TAG,
                "태그 강제 선택 = "
                        + tagName
        );

        Log.d(
                TAG,
                "현재 선택 태그 = "
                        + selectedTags
        );
    }

    private void clearButtonTint(
            Button button
    ) {

        if (button == null) {
            return;
        }

        button.setBackgroundTintList(
                null
        );
    }

    public String getTagsString() {

        return String.join(
                ",",
                selectedTags
        );
    }

    public List<String> getSelectedTags() {

        return selectedTags;
    }

    public void clear() {

        selectedTags.clear();
    }
}