package com.picsync_facebook_contact_sync.activity;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gary on 2015/8/31.
 */
public class Facebook_Utility extends Application {
    private AccessToken accessToken;
    private String return_data = null;
    public static final String user_data_broadcast = "com.facebook_sync_broadcast_user_data";

    public void getFacebookTagFriendData() {
        accessToken = AccessToken.getCurrentAccessToken();

        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {

                    //當RESPONSE回來的時候
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        GraphRequest request_tagFriend = new GraphRequest(
                                accessToken,
                                "/me/taggable_friends",
                                null,
                                HttpMethod.GET,
                                new GraphRequest.Callback() {
                                    public void onCompleted(GraphResponse response) {
                                        /* handle the result */
                                        JSONObject tagFriend_object = response.getJSONObject();
                                        String object_string = tagFriend_object.optString("data");

                                        /*return data*/
                                        sendMessage(object_string);
                                    }
                                }
                        );
                        Bundle params = new Bundle();
                        params.putString("limit","1000");
                        params.putString("fields", "id,name,picture.width(500).height(500)");
                        request_tagFriend.setParameters(params);
                        request_tagFriend.executeAsync();
                    }
                }
        );
        //包入你想要得到的資料 送出request
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link");
        request.setParameters(parameters);
        request.executeAsync();
    }

    // Send an Intent with an action named "custom-event-name". The Intent sent should
    // be received by the ReceiverActivity.
    private void sendMessage(String message) {
        Intent intent = new Intent(user_data_broadcast);
        // You can also include some extra data.
        intent.putExtra("facebook_return_data", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public String get_user_data_from_object_string(String object_string, int uid, String data_type) {
        JSONObject user_object = null;
        int tag_friend_length = 0;

        try {
            JSONArray jsonArray = new JSONArray(object_string);
            tag_friend_length = jsonArray.length();

            if (tag_friend_length > uid) {
                user_object = jsonArray.getJSONObject(uid);
            } else {
                return_data = null;
                return return_data;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        switch (data_type) {
            case "picture":
                try {
                    object_string = user_object.optString("picture");
                    JSONObject pic_object = new JSONObject(object_string);
                    JSONObject pic_url_object = new JSONObject(pic_object.optString("data"));
                    return_data = pic_url_object.optString("url");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "name":
                return_data = user_object.optString("name");
                break;
            default:
                break;
        }
        return return_data;
    }
}
