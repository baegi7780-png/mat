package com.tech.motjip.manager;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ReviewWriteValidator {

    private static final String TAG =
            "ReviewWriteValidatorDebug";

    private final Context context;

    public ReviewWriteValidator(
            Context context
    ) {

        this.context =
                context;
    }

    public boolean validate(
            Long placeId,
            Long memberId,
            int rating,
            String content
    ) {

        Log.d(
                TAG,
                "validate 시작"
        );

        Log.d(
                TAG,
                "placeId = "
                        + placeId
        );

        Log.d(
                TAG,
                "memberId = "
                        + memberId
        );

        Log.d(
                TAG,
                "rating = "
                        + rating
        );

        Log.d(
                TAG,
                "content length = "
                        + (
                        content == null
                                ? 0
                                : content.length()
                )
        );

        if (placeId == null
                || placeId == -1) {

            Log.e(
                    TAG,
                    "placeId 없음"
            );

            Toast.makeText(
                    context,
                    "장소 정보가 없습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        if (memberId == null
                || memberId == -1) {

            Log.e(
                    TAG,
                    "memberId 없음"
            );

            Toast.makeText(
                    context,
                    "로그인 사용자 정보가 없습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        if (rating <= 0) {

            Log.e(
                    TAG,
                    "rating 없음"
            );

            Toast.makeText(
                    context,
                    "별점을 선택해주세요.",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        if (content == null
                || content.trim().isEmpty()) {

            Log.e(
                    TAG,
                    "content 비어있음"
            );

            Toast.makeText(
                    context,
                    "후기 내용을 입력해주세요.",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        Log.d(
                TAG,
                "validate 통과"
        );

        return true;
    }
}