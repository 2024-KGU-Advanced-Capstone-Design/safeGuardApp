package com.example.safeguardapp;

import com.example.safeguardapp.Child.ChildFatalRequest;
import com.example.safeguardapp.Child.LocationSendRequest;
import com.example.safeguardapp.Emergency.CommentLoadRequest;
import com.example.safeguardapp.Emergency.CommentSendRequest;
import com.example.safeguardapp.Emergency.DeleteEmergencyRequest;
import com.example.safeguardapp.Emergency.EmergencyRequest;
import com.example.safeguardapp.Emergency.ReceivedEmergencyRequset;
import com.example.safeguardapp.Emergency.SentEmergencyRequest;
import com.example.safeguardapp.FindID.FindIDRequest;
import com.example.safeguardapp.FindID.FindIDResponse;
import com.example.safeguardapp.FindPW.CodeRequest;
import com.example.safeguardapp.FindPW.EmailRequest;
import com.example.safeguardapp.FindPW.ResetPwRequest;
import com.example.safeguardapp.Group.AddHelperRequest;
import com.example.safeguardapp.Group.AddParentRequest;
import com.example.safeguardapp.Group.CheckChildID;
import com.example.safeguardapp.Group.ChildDTO;
import com.example.safeguardapp.Group.ConfirmRequest;
import com.example.safeguardapp.Group.GetChildIDRequest;
import com.example.safeguardapp.Group.GetMemberIDRequest;
import com.example.safeguardapp.Group.RemoveHelperRequest;
import com.example.safeguardapp.Group.Sector.DangerSectorRequest;
import com.example.safeguardapp.Group.GroupRemoveRequest;
import com.example.safeguardapp.Group.MemberWithdrawRequest;
import com.example.safeguardapp.Group.ResetChildPWRequest;
import com.example.safeguardapp.Group.Sector.DeleteSectorRequest;
import com.example.safeguardapp.Group.Sector.SafeSectorRequest;
import com.example.safeguardapp.LogIn.LoginRequest;
import com.example.safeguardapp.LogIn.LoginResponse;
import com.example.safeguardapp.Notice.GetNotificationRequest;
import com.example.safeguardapp.Notice.GetNotificationResponse;
import com.example.safeguardapp.Map.LocationRequest;
import com.example.safeguardapp.Map.LocationResponse;
import com.example.safeguardapp.SignUp.CheckMemberID;
import com.example.safeguardapp.SignUp.SignUpRequestDTO;

import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserRetrofitInterface {
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("duplicate-check-member")
    Call<ResponseBody> memberIDCheck(@Body CheckMemberID jsonUser);

    @POST("duplicate-check-child")
    Call<ResponseBody> childIDCheck(@Body CheckChildID jsonUser);

    @POST("signup")
    Call<ResponseBody> signUp(@Body SignUpRequestDTO jsonUser);

    @POST("find-member-id")
    Call<FindIDResponse> findID(@Body FindIDRequest request);

    @POST("verification-email-request")
    Call<ResponseBody> sendCode(@Body EmailRequest jsonUser);

    @POST("verification-email")
    Call<ResponseBody> codeCheck(@Body CodeRequest jsonUser);

    @POST("reset-member-password")
    Call<ResponseBody> resetPw(@Body ResetPwRequest jsonUser);

    @POST("childsignup")
    Call<ResponseBody> child(@Body ChildDTO jsonUser);

    @POST("add-safe")
    Call<ResponseBody> sectorSafe(@Body SafeSectorRequest jsonUser);

    @POST("add-dangerous")
    Call<ResponseBody> sectorDanger(@Body DangerSectorRequest jsonUser);

    @POST("chose-child")
    Call<ResponseBody> childResetPW(@Body ResetChildPWRequest jsonUser);

    @POST("childremove")
    Call<ResponseBody> removeGroup(@Body GroupRemoveRequest jsonUser);

    @POST("memberremove")
    Call<ResponseBody> withdrawMember(@Body MemberWithdrawRequest jsonUser);

    @POST("update-coordinate")
    Call<ResponseBody> sendLocation(@Body LocationSendRequest locationSendRequest);

    @POST("return-coordinate")
    Call<LocationResponse> getLocation(@Body LocationRequest locationRequest);

//    @POST("return-coordinate")
//    Call<MemberLocationResponse> getMemberLocation(@Body MemberLocationRequest memberLocationRequest);

    @POST("read-areas")
    Call<ResponseBody> getSectorLocation(@Body RequestBody body);

    @POST("find-child-list")
    Call<ResponseBody> getChildID(@Body GetChildIDRequest request);

    @POST("find-member-by-child")
    Call<ResponseBody> getMemberID(@Body GetMemberIDRequest request);

    @POST("delete-area")
    Call<ResponseBody> deleteSector(@Body DeleteSectorRequest jsonUser);

    @POST("add-parent")
    Call<ResponseBody> addParent(@Body AddParentRequest request);

    @POST("addhelper")
    Call<ResponseBody> addHelper(@Body AddHelperRequest request);

    @POST("helperremove")
    Call<ResponseBody> removeHelper(@Body RemoveHelperRequest jsonUser);

    @POST("emergency")
    Call<ResponseBody> sendEmergency(@Body EmergencyRequest jsonUser);

    @POST("sent-emergency")
    Call<ResponseBody> sentEmergency(@Body SentEmergencyRequest request);

    @POST("sent-confirm")
    Call<ResponseBody> getNotification(@Body GetNotificationRequest request);

    @POST("received-emergency")
    Call<ResponseBody> getEmergency(@Body ReceivedEmergencyRequset request);

    @POST("send-confirm")
    Call<ResponseBody> sendConfirm(@Body ConfirmRequest request);

    @POST("fatal")
    Call<ResponseBody> sendFatal(@Body ChildFatalRequest request);

    @POST("write-comment")
    Call<ResponseBody> sendComment(@Body CommentSendRequest jsonUser);

    @POST("emergency-detail")
    Call<ResponseBody> loadComment(@Body CommentLoadRequest request);

    @POST("delete-comment")
    Call<ResponseBody> deleteEmergency(@Body DeleteEmergencyRequest jsonUser);
}
