/*
 * Copyright 2013 Peng fei Pan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.xiaopan.easy.imageloader.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;

@SuppressWarnings("deprecation")
public class ImageGalleryFragment extends TitleFragment {
	public static final String PARAM_REQUIRED_STRING_NAME = "PARAM_REQUIRED_STRING_NAME";
	public static final String PARAM_REQUIRED_STRING_ARRAY_URLS = "PARAM_REQUIRED_STRING_ARRAY_URLS";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Gallery gallery = (Gallery) new Gallery(getActivity());
		gallery.setAdapter(new ImageAdapter(getActivity(), getArguments().getStringArray(PARAM_REQUIRED_STRING_ARRAY_URLS)));
		return gallery;
	}

	@Override
	public String getTitle() {
		return getArguments().getString(PARAM_REQUIRED_STRING_NAME);
	}
}
