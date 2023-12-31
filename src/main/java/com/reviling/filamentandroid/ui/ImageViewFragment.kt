package com.reviling.filamentandroid.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.reviling.filamentandroid.databinding.FragmentImageBinding
import com.reviling.filamentandroid.utils.rotate
import com.reviling.filamentandroid.utils.toBitmap

@ExperimentalGetImage class ImageViewFragment(
    private var imageProxy: ImageProxy
) : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(image: ImageProxy) =
            ImageViewFragment(image)
    }

    private lateinit var _binding: FragmentImageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageProxy.image?.let {
            _binding.imageView.setImageBitmap(it.toBitmap().rotate(imageProxy.imageInfo.rotationDegrees.toFloat()))
        }
    }
}