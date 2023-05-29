package com.areeb.datagetterfromusb

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.areeb.datagetterfromusb.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var _binding: ActivityMainBinding
    private val binding get() = _binding
    private lateinit var weightMachineManager: WeightMachineManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding.root)
        weightMachineManager = WeightMachineManager(this)
//        weightMachineManager.startListening()
        binding.weightText.setOnClickListener {
            weightMachineManager.startListening()
        }
        binding.close.setOnClickListener {
            weightMachineManager.stopListening()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        weightMachineManager.stopListening()
    }
}
