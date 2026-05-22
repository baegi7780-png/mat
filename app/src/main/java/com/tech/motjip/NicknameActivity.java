package com.tech.motjip;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tech.motjip.Controller.NicknameController;
import com.tech.motjip.Dto.ResponseDto.LoginResponseDto;
import com.tech.motjip.Utils.DialogUtil;
import com.tech.motjip.Utils.LoginStateManager;

public class NicknameActivity extends AppCompatActivity
        implements NicknameController.NicknameControllerCallback {

    private static final String TAG = "NicknameActivityDebug";

    private Long memberId;
    private String nickname;

    private EditText etNickname;
    private Button btnSubmit;

    private NicknameController nicknameController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nickname);

        nicknameController = new NicknameController(this, this);

        Log.d(TAG, "onCreate() - NicknameActivity started");

        try {
            memberId = getIntent().getLongExtra("member_id", -1L);
            nickname = getIntent().getStringExtra("nickname");

            if (nickname != null && !nickname.trim().isEmpty()) {

                LoginStateManager.setLoginStatus(
                        this,
                        LoginStateManager.LOGIN
                );

                Intent homeIntent = new Intent(this, HomeActivity.class);
                startActivity(homeIntent);
                finish();
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Intent 처리 오류", e);
        }

        etNickname = findViewById(R.id.etNickname);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> {

            String inputNickname =
                    etNickname.getText()
                            .toString()
                            .trim();

            if (inputNickname.isEmpty()) {

                Toast.makeText(
                        this,
                        "닉네임을 입력해 주세요.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (inputNickname.length() < 2
                    || inputNickname.length() > 10) {

                Toast.makeText(
                        this,
                        "닉네임은 2~10자 이내로 입력해 주세요.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // 특수문자 제한 검사
            if (!inputNickname.matches("^[a-zA-Z0-9가-힣]+$")) {

                DialogUtil.showMessageDialog(
                        this,
                        R.drawable.fail,
                        "닉네임 오류",
                        "닉네임에는 특수문자를 사용할 수 없습니다.",
                        null
                );

                return;
            }

            nicknameController.updateNickname(
                    memberId,
                    inputNickname
            );
        });
    }

    @Override
    public void onNicknameSuccess(
            LoginResponseDto user
    ) {

        // 로그인 상태 저장
        LoginStateManager.setLoginStatus(
                this,
                LoginStateManager.LOGIN
        );

        DialogUtil.showMessageDialog(
                this,
                R.drawable.success,
                "설정 완료",
                "반가워요! 닉네임 설정이 완료되었습니다.",
                () -> {

                    Intent homeIntent =
                            new Intent(
                                    NicknameActivity.this,
                                    HomeActivity.class
                            );

                    homeIntent.putExtra(
                            "LOGIN_USER_INFO",
                            user
                    );

                    startActivity(homeIntent);

                    finish();
                }
        );
    }

    @Override
    public void onNicknameFail(
            String message
    ) {

        DialogUtil.showMessageDialog(
                this,
                R.drawable.fail,
                "설정 실패",
                message,
                null
        );
    }
}