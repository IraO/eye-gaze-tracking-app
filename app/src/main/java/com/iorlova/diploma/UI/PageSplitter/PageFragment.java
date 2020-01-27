package com.iorlova.diploma.UI.PageSplitter;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.iorlova.diploma.R;

public class PageFragment extends Fragment {

    private final static String PAGE_TEXT = "PAGE_TEXT";

    public static PageFragment newInstance(CharSequence pageText) {
        PageFragment fragment = new PageFragment();

        Bundle args = new Bundle();
        args.putCharSequence(PAGE_TEXT, pageText);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        CharSequence text = getArguments().getCharSequence(PAGE_TEXT);
        TextView pageView = (TextView) inflater.inflate(R.layout.book_page, container, false);
        pageView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size));
        pageView.setText(text);
        return pageView;
    }
}
