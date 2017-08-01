package com.example.jin.hellofengmap;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonInfoActivity extends AppCompatActivity {

    final String Tag="PersonInfoActivity";

    public static final int CHOOSE_PHOTO=2;

    private String userName=new String();

    private String userPhone=new String();

    private String userGender=new String();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_info);
        userName=MainActivity.mainName;
        userPhone=MainActivity.mainPhone;
        userGender=MainActivity.mainGender;
        //更换头像textview的点击事件
        TextView change_head=(TextView) findViewById(R.id.change_head);
        change_head.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //检查是否具有权限：没有，申请；有，打开相册
                if(ContextCompat.checkSelfPermission(PersonInfoActivity.this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                        PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(PersonInfoActivity.this,new
                    String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else{
                    openAlbum();
                }



            }
        });

        final TextView nickName=(TextView)findViewById(R.id.nick_name);
        nickName.setText(MainActivity.mainName);
        final TextView gender=(TextView)findViewById(R.id.gender);
        gender.setText(MainActivity.mainGender);
        final TextView phoneNumber=(TextView)findViewById(R.id.phone_number);
        phoneNumber.setText(MainActivity.mainPhone);
        //昵称的点击事件
        nickName.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                final EditText editText=new EditText(PersonInfoActivity.this);
                AlertDialog.Builder builder=new AlertDialog.Builder(PersonInfoActivity.this);
                builder.setTitle("输入昵称");
                builder.setView(editText);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener(){
                    //确定按钮的点击事件
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        nickName.setText(editText.getText().toString());
                        userName=editText.getText().toString();
                    }});
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    //取消按钮的点击事件
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }});
                builder.show();

            }
        });

        gender.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                final String[] array = new String[] { "男","女" };

                Dialog alertDialog = new AlertDialog.Builder(PersonInfoActivity.this)
                        .setTitle("选择性别")
                        .setItems(array, new DialogInterface.OnClickListener() {
                            //性别选择的点击事件
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                gender.setText(array[which]);
                                userGender=array[which];
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                }
                        })
                        .create();
                alertDialog.show();
            }
        });

        phoneNumber.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                final EditText editText=new EditText(PersonInfoActivity.this);
                AlertDialog.Builder builder=new AlertDialog.Builder(PersonInfoActivity.this);
                builder.setTitle("电话");
                builder.setView(editText);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener(){
                    //确定按钮的点击事件
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        phoneNumber.setText(editText.getText().toString());
                        userPhone=editText.getText().toString();
                    }});
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    //取消按钮的点击事件
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }});

                builder.show();
            }
        });

        //保存按钮的点击事件
        Button saveInfo=(Button)findViewById(R.id.save_info);
        saveInfo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(userName.length()>10||userName.length()==0)
                    Toast.makeText(PersonInfoActivity.this,"昵称过长，请重新输入",Toast.LENGTH_SHORT).show();
                else if(userGender.length()!=1)
                    Toast.makeText(PersonInfoActivity.this,"请选择性别",Toast.LENGTH_SHORT).show();
                else if(userPhone.length()!=11 || userPhone.charAt(0)!='1')
                    Toast.makeText(PersonInfoActivity.this,"请输入正确的手机号",Toast.LENGTH_SHORT).show();
                else{
                    //输入合法的情况下,将信息传给MainActivity
                    MainActivity.mainName=userName;
                    MainActivity.mainGender=userGender;
                    MainActivity.mainPhone=userPhone;
                    finish();
                }
            }
        });
    }

    /**
     * 打开相册
     */
    private void openAlbum(){
        Intent intent=new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,CHOOSE_PHOTO);
        Log.d(Tag,"成功打开相册");
    }

    //申请权限的回调事件

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 1:
                //授予权限，打开相册
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    openAlbum();
                else{
                    Toast.makeText(PersonInfoActivity.this,"你拒绝了权限申请",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 从相册选取完照片回调函数
     *
     * @param requestCode
     * @param resultCode
     * @param data
     *
     * created by Zhaoguo Wang in
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case CHOOSE_PHOTO:
                Log.d(Tag,"CHOOSE_PHOTO");
                if (resultCode == RESULT_OK){
                    Log.d(Tag,"成功选取图片");
                    //判断手机系统版本号
                    if(Build.VERSION.SDK_INT >= 19){
                        //4.4及以上系统处理方法
                        handleImageOnkitKat(data);
                    }else{
                        Toast.makeText(PersonInfoActivity.this,"below",Toast.LENGTH_SHORT).show();
                    }
                }
        }

    }

    @TargetApi(19)
    private void handleImageOnkitKat(Intent data){
        Log.d(Tag,"处理选择的图片");
        String imagePath=null;
        Uri uri=data.getData();
        if(DocumentsContract.isDocumentUri(this,uri)){
            //如果是document类型的uri，通过document id进行处理
            Log.d(Tag,"document类型的uri");
            String docId=DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id=docId.split(":")[1];  //解析出数字格式的id
                String selection= MediaStore.Images.Media._ID+"="+id;
                imagePath=getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content:" +
                        "//downloads/public_downloads"), Long.valueOf(docId));
                imagePath=getImagePath(contentUri,null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            Log.d(Tag,"content类型的uri");
            //如果是content类型的uri，使用普通方法处理
            imagePath=getImagePath(uri,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            Log.d(Tag,"file类型的uri");
            //如果是file类型的uri，直接获取图片路径即可
            imagePath=uri.getPath();
        }
        if(imagePath==null)
            Log.d(Tag,"handleImageKitKat获取路径失败");
        //更换头像
        sendPath(imagePath);
    }

    private String getImagePath(Uri uri,String selection){
        String path=null;
        //通过uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        if(path==null)
            Log.d(Tag,"getImagePath获取路径失败");
        return path;
    }

    /**
     * 将路径发给上一个活动
     * @param imagePath
     */
    private void sendPath(String imagePath){
        if(imagePath==null) {
            Log.d(Tag, "获取路径失败");
            return;
        }
        else {
            Log.d(Tag, imagePath);
            Intent intent = new Intent();
            intent.putExtra("imagePath",imagePath);
            setResult(RESULT_OK,intent);
            //在PersonInfoActivity被销毁之后，会回调上一个活动的onActivityResult
            finish();
        }
    }
}
