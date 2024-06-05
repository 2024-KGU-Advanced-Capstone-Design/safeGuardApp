package com.example.safeguardapp.Child;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.safeguardapp.FindPW.EmailRequest;
import com.example.safeguardapp.LogIn.LoginPageFragment;
import com.example.safeguardapp.MainActivity;
import com.example.safeguardapp.PreferenceManager;
import com.example.safeguardapp.R;
import com.example.safeguardapp.RetrofitClient;
import com.example.safeguardapp.Setting.ChangeNicknameRequest;
import com.example.safeguardapp.Setting.LoadImageRequest;
import com.example.safeguardapp.Setting.SendImageRequest;
import com.example.safeguardapp.Setting.SettingFragment;
import com.example.safeguardapp.StartScreenActivity;
import com.example.safeguardapp.UserRetrofitInterface;
import com.google.gson.Gson;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChildSettingFragment extends Fragment {
    private Button changeName, logout;
    private String newNickname, loadfilepath;
    private CircleImageView changeImage;
    private static final int REQUEST_IMAGE_SELECT = 1;
    private RetrofitClient retrofitClient;
    private UserRetrofitInterface userRetrofitInterface;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_child_setting, container, false);

        initializeView(view);
        setupListeners();

        return view;
    }

    private void initializeView(View view) {
        changeName = view.findViewById(R.id.editName_btn);
        logout = view.findViewById(R.id.logout_btn);
        changeImage = view.findViewById(R.id.imageView);

        retrofitClient = RetrofitClient.getInstance();
        userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();
    }

    private void setupListeners() {
        changeName.setOnClickListener(v -> changeNameMethod());
        logout.setOnClickListener(v -> logoutMethod());
        changeImage.setOnClickListener(v -> changeImageMethod());
        loadImageToServer();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textView = view.findViewById(R.id.childId);
        textView.setText(LoginPageFragment.saveID);

        // SettingFragment에서 뒤로 갔을 때 MapFragment로 이동
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 뒤로 가기 시 실행되는 코드
                previous();
            }
        });

        LinearLayout linearLayout = view.findViewById(R.id.child_setting_screen);
        YoYo.with(Techniques.FadeIn).duration(700).repeat(0).playOn(linearLayout);
    }

    private void changeNameMethod() {
        LayoutInflater inflater2 = getLayoutInflater();
        View dialogView2 = inflater2.inflate(R.layout.edit_update_nickname, null);

        EditText boxInDialog2 = dialogView2.findViewById(R.id.nickname_editText);

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("작성할 내용을 입력해주세요.")
                .setView(dialogView2) // 커스텀 레이아웃 설정
                .setPositiveButton("확인", (dialog, which) -> {

                    // OK 버튼 클릭 시 처리할 코드
                    newNickname = boxInDialog2.getText().toString();
                    nicknameChange();
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    // Cancel 버튼 클릭 시 처리할 코드
                    dialog.dismiss();
                });

        // 다이얼로그 표시
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void nicknameChange() {
        ChangeNicknameRequest changeNicknameRequest = new ChangeNicknameRequest(LoginPageFragment.saveID, newNickname);
        Call<ResponseBody> call = userRetrofitInterface.changeNickname(changeNicknameRequest);

        call.clone().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "통신 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutMethod() {
        new AlertDialog.Builder(requireContext())
                .setTitle("로그아웃")
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Context context = getActivity();
                        if (context != null) {
                            Intent serviceIntent = new Intent(context, LocationService.class);
                            context.stopService(serviceIntent);
                        } else {
                            Log.e("MyFragment", "Context is null, cannot stop service");
                        }
                        PreferenceManager.clear(getContext());
                        transScreen();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void changeImageMethod(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_SELECT);
    }

    // crop 시작 메서드
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_SELECT && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                CropImage.activity(selectedImageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .setAspectRatio(1, 1)
                        .start(getContext(), this);
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    Log.e("POST", "try 실행중");
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), resultUri);
                    Bitmap circularBitmap = getCircularBitmap(bitmap);
                    saveImage(circularBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "이미지를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                // 오류 처리
                Toast.makeText(getContext(), "이미지 자르기 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 이미지 원형으로 바꿈
    private Bitmap getCircularBitmap(Bitmap bitmap) {
        Log.e("POST", "getCircular 실행중");
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int minEdge = Math.min(width, height);

        Bitmap output = Bitmap.createBitmap(minEdge, minEdge, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, minEdge, minEdge);

        float r = minEdge / 2f;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    //이미지 저장 및 서버에 이미지 데이터 전달
    private void saveImage(Bitmap bitmap) {
        RetrofitClient retrofitClient = RetrofitClient.getInstance();
        UserRetrofitInterface userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();

        // 파일을 저장할 경로 설정
        File file = new File(getActivity().getFilesDir(), "circular_image.png");

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            Toast.makeText(getActivity(), "이미지가 저장되었습니다.", Toast.LENGTH_SHORT).show();

            SendImageRequest sendImageRequest = new SendImageRequest("Member", LoginPageFragment.saveID);

            Gson gson = new Gson();
            String json = gson.toJson(sendImageRequest);
            RequestBody dto = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);

            // 이미지 파일 준비
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/png"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), reqFile);


            Call<ResponseBody> call = userRetrofitInterface.uploadFile(dto, body);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        refreshFragment();
                    } else {

                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getActivity(), "업로드 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("POST", "이미지 전송 오류");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "이미지를 저장할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    //이미지 경로 불러오기 및 이미지 설정
    private void loadImageToServer() {
        LoadImageRequest loadImageRequest = new LoadImageRequest("Member", LoginPageFragment.saveID);
        Call<ResponseBody> call = userRetrofitInterface.getloadFile(loadImageRequest);
        String fileUrl = "http://223.130.152.254:8080/imagePath/";
        call.clone().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String reponseBodyString = response.body().string();
                        JSONObject json = new JSONObject(reponseBodyString);
                        for(Iterator<String> keys = json.keys(); keys.hasNext(); ) {
                            String key = keys.next();
                            String value = json.getString(key);
                            if (key.equals("filePath")) {
                                loadfilepath = value;
                                Log.e("POST", loadfilepath);
                                Glide.with(getActivity())
                                        .load(fileUrl + loadfilepath)
                                        .into(changeImage);
                            }
                            else {
                                continue;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void refreshFragment() {
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.containers, new SettingFragment()); // 여기서 R.id.containers는 프래그먼트가 들어있는 컨테이너의 ID입니다.
        fragmentTransaction.commit();
    }

    private void previous(){
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        // 이동 시에는 이미 생성된 mapFragment를 사용하여 교체
        transaction.replace(R.id.containers, ((ChildMainActivity) requireActivity()).childMapFragment);
        transaction.commit();
    }

    private void transScreen(){
        Intent intent = new Intent(getActivity(), StartScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
