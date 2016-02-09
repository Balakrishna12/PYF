package com.pyt.postyourfun.Fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.pyt.postyourfun.Adapter.GridViewImageAdapter;
import com.pyt.postyourfun.Adapter.GridViewImageInterface;
import com.pyt.postyourfun.R;
import com.pyt.postyourfun.Utils.UserImageSQLiteHelper;
import com.pyt.postyourfun.Utils.UsersImageModel;
import com.pyt.postyourfun.constants.Constants;
import com.pyt.postyourfun.social.SocialController;
import com.pyt.postyourfun.social.SocialControllerInterface;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Photo;
import com.sromku.simple.fb.entities.Privacy;
import com.sromku.simple.fb.listeners.OnPublishListener;

import java.io.File;
import java.util.ArrayList;

public class ViewImageFragment extends Fragment implements View.OnClickListener, GridViewImageInterface, SocialControllerInterface {

    private GridView imageGrid;
    private Button btnShareFriend;
    private ImageView expandedImageView;
    private FrameLayout containerView;

    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;

    private Context context;
    private GridViewImageAdapter gridViewImageAdapter;
    private UserImageSQLiteHelper imageSQLiteHelper;
    private ArrayList<UsersImageModel> userImages = new ArrayList<>();

    private int windowWidth;
    private int windowHeight;
    private ProgressDialog progressDialog;

    private SocialController _socialController = null;

    public static ViewImageFragment newInstance() {
        ViewImageFragment fragment = new ViewImageFragment();
        return fragment;
    }

    public ViewImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        imageSQLiteHelper = new UserImageSQLiteHelper(getActivity());
        _socialController = SocialController.sharedInstance(getActivity(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_image, container, false);

        imageGrid = (GridView) view.findViewById(R.id.image_gridView);
        btnShareFriend = (Button) view.findViewById(R.id.btn_shareFriends);
        expandedImageView = (ImageView) view.findViewById(R.id.expanedImageView);
        containerView = (FrameLayout) view.findViewById(R.id.container);

        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        btnShareFriend.setOnClickListener(this);
        gridViewImageAdapter = new GridViewImageAdapter(getActivity(), userImages, this);
        imageGrid.setAdapter(gridViewImageAdapter);

        WindowManager wm = (WindowManager) getActivity().getSystemService(context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        windowWidth = size.x;
        windowHeight = size.y;
        initView();

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading data...");
        progressDialog.setCancelable(false);

        Log.d("View Fragment: ", "View Created");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (_socialController != null) {
            _socialController.onResume();
        }

        initView();
        Log.d("View Fragment: ", "Resumed");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (_socialController != null) {
            _socialController.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (_socialController != null) {
            _socialController.onDestroy();
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_shareFriends:
                if (gridViewImageAdapter.getSelectedPosition().isEmpty()) {
                    Toast.makeText(getActivity(), "Please select image", Toast.LENGTH_SHORT).show();
                } else {
                    int position = gridViewImageAdapter.getSelectedPosition().get(0);
                    SharePhotoContent content = new SharePhotoContent.Builder().build();
                    ShareDialog shareDialog = new ShareDialog(getActivity());
                    File purposeFile = new File(userImages.get(position).getLocalPath());
                    if (purposeFile.exists()) {
                        if (shareDialog.canShow(content)) {
                            Photo photo = new Photo.Builder()
                                    .setImage(BitmapFactory.decodeFile(purposeFile.getPath()))
                                    .build();
                            SimpleFacebook.getInstance().publish(photo, true, null);
                        } else {
                            Privacy privacy = new Privacy.Builder().setPrivacySettings(Privacy.PrivacySettings.ALL_FRIENDS).build();
                            Photo photo = new Photo.Builder()
                                    .setImage(BitmapFactory.decodeFile(purposeFile.getPath()))
                                    .setPrivacy(privacy)
                                    .build();
                            SimpleFacebook.getInstance().publish(photo, false, new OnPublishListener() {

                                @Override
                                public void onException(Throwable throwable) {
                                    if (progressDialog != null) {
                                        progressDialog.dismiss();
                                    }
                                }

                                @Override
                                public void onFail(String reason) {
                                    if (progressDialog != null) {
                                        progressDialog.dismiss();
                                    }
                                }

                                @Override
                                public void onThinking() {
                                    progressDialog.show();
                                }

                                @Override
                                public void onComplete(String response) {
                                    if (progressDialog != null) {
                                        progressDialog.dismiss();
                                    }
                                }
                            });
                        }
                    } else {
                        _socialController.shareWithFaceBook(ViewImageFragment.this,
                                "",
                                "",
                                userImages.get(position).getImageUrl(),
                                userImages.get(position).getImageUrl());
                    }
                }
                break;
        }
    }

    public void initView() {
        userImages.clear();
        userImages.addAll(imageSQLiteHelper.getAllImages());
        gridViewImageAdapter.notifyDataSetChanged();
    }

    private void zoomImageFromThumb(final View thumbView, String imageId) {

        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        loadImage(imageId);

        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        thumbView.getGlobalVisibleRect(startBounds);
        containerView.getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        float startScale;
        if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top))
                        .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        expandedImageView.destroyDrawingCache();
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        expandedImageView.destroyDrawingCache();
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }

    @Override
    public void onClickedImage(View v, int index) {
        zoomImageFromThumb(v, userImages.get(index).getImageId());
//        loadImage(userImages.get(index).getImageId());
    }

    @Override
    public void onChangeCheckbox(boolean isChecked) {

    }

    private void loadImage(String imageId) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        // Don't read the pixel array into memory, only read the picture information
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(Constants.IMAGE_FULL_PATH + "/" + imageId + ".jpg", opts);
        // Get a picture from the Options resolution
        int imageHeight = opts.outHeight;
        int imageWidth = opts.outWidth;

        // Calculation of sampling rate
        int scaleX = imageWidth / windowWidth;
        int scaleY = imageHeight / windowHeight;
        int scale = 1;
        // The sampling rate in accordance with the direction of maximum prevail
        if (scaleX > scaleY && scaleY >= 1) {
            scale = scaleX;
        }
        if (scaleX < scaleY && scaleX >= 1) {
            scale = scaleY;
        }

        // False read the image pixel array into memory, in accordance with the sampling rate set
        opts.inJustDecodeBounds = false;
        // Sampling rate
        opts.inSampleSize = scale;
        Bitmap bitmap = BitmapFactory.decodeFile(Constants.IMAGE_FULL_PATH + "/" + imageId + ".jpg", opts);
        expandedImageView.setImageBitmap(bitmap);
    }

    @Override
    public void onSuccess(int type, int action) {

    }

    @Override
    public void onFailure(int type, int action) {

    }
}
