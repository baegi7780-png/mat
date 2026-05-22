package com.tech.motjip;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tech.motjip.API.RetrofitClient;
import com.tech.motjip.Dto.RequestDto.UpdateMyNicknameRequestDto;
import com.tech.motjip.Dto.ResponseDto.LoginResponseDto;
import com.tech.motjip.Utils.DialogUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditNicknameActivity extends AppCompatActivity {

    private EditText etNickname;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_nickname);

        etNickname = findViewById(R.id.etNickname);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> {
            String inputNickname = etNickname.getText().toString().trim();

            if (inputNickname.isEmpty()) {
                Toast.makeText(this, "닉네임을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (inputNickname.length() < 2 || inputNickname.length() > 10) {
                Toast.makeText(this, "닉네임은 2~10자 이내로 입력해 주세요.", Toast.LENGTH_SHORT).show();
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

            UpdateMyNicknameRequestDto requestDto = new UpdateMyNicknameRequestDto(inputNickname);

            RetrofitClient.getApiService(this)
                    .updateMyNickname(requestDto)
                    .enqueue(new Callback<LoginResponseDto>() {
                        @Override
                        public void onResponse(Call<LoginResponseDto> call, Response<LoginResponseDto> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                // 성공 모달 표시 후 확인 버튼을 눌러야 finish()
                                DialogUtil.showMessageDialog(
                                        EditNicknameActivity.this,
                                        R.drawable.success,
                                        "수정 완료",
                                        "닉네임이 성공적으로 변경되었습니다.",
                                        () -> finish()
                                );
                            } else {
                                DialogUtil.showMessageDialog(
                                        EditNicknameActivity.this,
                                        R.drawable.fail,
                                        "수정 실패",
                                        "이미 사용 중인 닉네임이거나 오류가 발생했습니다.",
                                        null
                                );
                            }
                        }

                        @Override
                        public void onFailure(Call<LoginResponseDto> call, Throwable t) {
                            DialogUtil.showMessageDialog(
                                    EditNicknameActivity.this,
                                    R.drawable.fail,
                                    "연결 오류",
                                    "서버와의 통신이 원활하지 않습니다.",
                                    null
                            );
                        }
                    });
        });
    }
}