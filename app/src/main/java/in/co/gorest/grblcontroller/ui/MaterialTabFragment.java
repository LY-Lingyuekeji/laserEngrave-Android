/*
 *  /**
 *  * Copyright (C) 2017  Grbl Controller Contributors
 *  *
 *  * This program is free software; you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation; either version 2 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, write to the Free Software Foundation, Inc.,
 *  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *  * <http://www.gnu.org/licenses/>
 *
 */

package in.co.gorest.grblcontroller.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.activity.MaterialActivity;
import in.co.gorest.grblcontroller.adapters.MaterialAdapter;

import in.co.gorest.grblcontroller.databinding.FragmentMaterialTabBinding;
import in.co.gorest.grblcontroller.events.UiToastEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.listeners.FileSenderListener;
import in.co.gorest.grblcontroller.listeners.MachineStatusListener;
import in.co.gorest.grblcontroller.model.EffectBean;
import in.co.gorest.grblcontroller.model.GcodesBean;
import in.co.gorest.grblcontroller.model.PictureBean;
import in.co.gorest.grblcontroller.util.FileManager;
import in.co.gorest.grblcontroller.util.FileUtils;
import in.co.gorest.grblcontroller.util.GcodeResults;
import in.co.gorest.grblcontroller.util.Image2Gcode;
import in.co.gorest.grblcontroller.util.ImageProcess;
import in.co.gorest.grblcontroller.util.ImgUtil;
import in.co.gorest.grblcontroller.util.PictureUtil;
import in.co.gorest.grblcontroller.util.ScreenInchUtils;
import in.co.gorest.grblcontroller.util.ZoomViewBean;


public class MaterialTabFragment extends BaseFragment {

    private static final String TAG = MaterialTabFragment.class.getSimpleName();

    private MachineStatusListener machineStatus;
    private FileSenderListener fileSender;
    private EnhancedSharedPreferences sharedPref;

    public MaterialTabFragment() {
    }

    public static MaterialTabFragment newInstance() {
        return new MaterialTabFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        machineStatus = MachineStatusListener.getInstance();
        fileSender = FileSenderListener.getInstance();
        sharedPref = EnhancedSharedPreferences.getInstance(requireActivity().getApplicationContext(), getString(R.string.shared_preference_key));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        FragmentMaterialTabBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_material_tab, container, false);
        binding.setMachineStatus(machineStatus);
        binding.setFileSender(fileSender);
        View view = binding.getRoot();

        // 素材
        LinearLayout llMaterial = view.findViewById(R.id.ll_material);
        llMaterial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), MaterialActivity.class));
            }
        });

        // 相册
        LinearLayout llAlbum = view.findViewById(R.id.ll_album);
        llAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }


    private Bitmap getBitmap(int inSampleSize, Uri uri) throws FileNotFoundException {
        InputStream input = getActivity().getContentResolver().openInputStream(uri);
        BitmapFactory.Options mOptions = new BitmapFactory.Options();
        mOptions.inJustDecodeBounds = false;
        mOptions.inSampleSize = inSampleSize;
        mOptions.inPreferredConfig = Bitmap.Config.RGB_565;//optional
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, mOptions);
//        return bitmap;
        if (bitmap.getWidth() >= 1000 || bitmap.getHeight() >= 1000) {
            return getBitmap(inSampleSize + 1, uri);
        } else {
            bitmap = PictureUtil.rotaingImageView(PictureUtil.readPictureDegree(FileUtils.getPathFromUri(getActivity(), uri)), bitmap);
            return bitmap;
        }
    }


    private void saveSuperpositionUri(GcodesBean gcodesBean) {
        try {

            Bitmap canvasBitmap = Bitmap.createBitmap(1360, 1370, Bitmap.Config.RGB_565);
            canvasBitmap = Bitmap.createBitmap(canvasBitmap, 0, 1190, 162, 180);
            gcodesBean.setSuperpositionUri(ImgUtil.saveBitmap("8_canvas_" + System.currentTimeMillis() + ".png", canvasBitmap).getPath());
            FileManager.get().addDelPath(gcodesBean.getSuperpositionUri());

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "saveSuperpositionUri e:" + e.getMessage());
        }

    }
}

