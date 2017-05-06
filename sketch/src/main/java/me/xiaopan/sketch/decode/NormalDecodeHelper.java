/*
 * Copyright (C) 2017 Peng fei Pan <sky@xiaopan.me>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xiaopan.sketch.decode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import me.xiaopan.sketch.SLog;
import me.xiaopan.sketch.SLogType;
import me.xiaopan.sketch.SketchMonitor;
import me.xiaopan.sketch.cache.BitmapPool;
import me.xiaopan.sketch.cache.BitmapPoolUtils;
import me.xiaopan.sketch.drawable.ImageAttrs;
import me.xiaopan.sketch.feature.ImageSizeCalculator;
import me.xiaopan.sketch.feature.ProcessedImageCache;
import me.xiaopan.sketch.request.LoadRequest;
import me.xiaopan.sketch.request.MaxSize;
import me.xiaopan.sketch.util.SketchUtils;

public class NormalDecodeHelper implements DecodeHelper {
    private static final String LOG_NAME = "NormalDecodeHelper";

    @Override
    public boolean match(LoadRequest request, DataSource dataSource, ImageType imageType,
                         BitmapFactory.Options boundOptions) {
        return true;
    }

    @Override
    public DecodeResult decode(LoadRequest request, DataSource dataSource, ImageType imageType,
                               BitmapFactory.Options boundOptions, BitmapFactory.Options decodeOptions, int orientation) {
        decodeOptions.outWidth = boundOptions.outWidth;
        decodeOptions.outHeight = boundOptions.outHeight;
        decodeOptions.outMimeType = boundOptions.outMimeType;

        // Calculate inSampleSize according to max size
        MaxSize maxSize = request.getOptions().getMaxSize();
        if (maxSize != null) {
            boolean supportLargeImage = SketchUtils.supportLargeImage(request, imageType);
            ImageSizeCalculator imageSizeCalculator = request.getSketch().getConfiguration().getImageSizeCalculator();
            decodeOptions.inSampleSize = imageSizeCalculator.calculateInSampleSize(boundOptions.outWidth, boundOptions.outHeight,
                    maxSize.getWidth(), maxSize.getHeight(), supportLargeImage);
        }

        // Set inBitmap from bitmap pool
        if (BitmapPoolUtils.sdkSupportInBitmap() && !request.getOptions().isBitmapPoolDisabled()) {
            BitmapPool bitmapPool = request.getSketch().getConfiguration().getBitmapPool();
            BitmapPoolUtils.setInBitmapFromPool(decodeOptions, bitmapPool);
        }

        Bitmap bitmap = null;
        try {
            bitmap = DefaultImageDecoder.decodeBitmap(dataSource, decodeOptions);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();

            // 要是因为inBitmap而解码失败就记录日志并再此尝试
            if (BitmapPoolUtils.sdkSupportInBitmap()) {
                if (!request.getOptions().isBitmapPoolDisabled() && decodeOptions.inBitmap != null) {
                    SketchMonitor sketchMonitor = request.getSketch().getConfiguration().getMonitor();

                    BitmapPool bitmapPool = request.getSketch().getConfiguration().getBitmapPool();
                    BitmapPoolUtils.inBitmapThrow(e, decodeOptions, sketchMonitor, bitmapPool, request.getUri(), boundOptions.outWidth, boundOptions.outHeight);

                    decodeOptions.inBitmap = null;
                    try {
                        bitmap = DefaultImageDecoder.decodeBitmap(dataSource, decodeOptions);
                    } catch (Throwable error) {
                        error.printStackTrace();
                        sketchMonitor.onDecodeNormalImageError(error, request, boundOptions.outWidth, boundOptions.outHeight, boundOptions.outMimeType);
                    }
                }
            }
        } catch (Throwable error) {
            error.printStackTrace();
            SketchMonitor sketchMonitor = request.getSketch().getConfiguration().getMonitor();
            sketchMonitor.onDecodeNormalImageError(error, request, boundOptions.outWidth, boundOptions.outHeight, boundOptions.outMimeType);
        }

        // 过滤掉无效的图片
        if (bitmap == null || bitmap.isRecycled()) {
            dataSource.onDecodeError();
            return null;
        }

        // 过滤宽高小于等于1的图片
        if (bitmap.getWidth() <= 1 || bitmap.getHeight() <= 1) {
            SLog.w(SLogType.REQUEST, LOG_NAME, "image width or height less than or equal to 1px. imageSize: %dx%d. bitmapSize: %dx%d. %s",
                    boundOptions.outWidth, boundOptions.outHeight, bitmap.getWidth(), bitmap.getHeight(), request.getKey());
            bitmap.recycle();
            dataSource.onDecodeError();
            return null;
        }

        // 成功
        dataSource.onDecodeSuccess(bitmap, boundOptions.outWidth, boundOptions.outHeight, boundOptions.outMimeType, decodeOptions.inSampleSize);

        ProcessedImageCache processedImageCache = request.getConfiguration().getProcessedImageCache();
        boolean processed = processedImageCache.canUseCacheProcessedImageInDisk(decodeOptions.inSampleSize);

        ImageAttrs imageAttrs = new ImageAttrs(boundOptions.outMimeType, boundOptions.outWidth, boundOptions.outHeight, orientation);
        return new DecodeResult(imageAttrs, bitmap).setProcessed(processed);
    }
}