package com.mrmannwood.hexlauncher.nux

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.role.RoleManagerCompat
import androidx.fragment.app.Fragment
import com.mrmannwood.hexlauncher.role.RoleManagerHelper
import com.mrmannwood.launcher.R

class SetAsHomeFragment : Fragment(R.layout.fragment_nux_set_home) {

    private val setHomeLauncherResultContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ -> requireActivity().finish() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.set_as_home).apply {
            setOnClickListener {
                val (intent, func) = RoleManagerHelper.INSTANCE.getRoleSetIntent(
                    requireActivity(),
                    RoleManagerCompat.ROLE_HOME
                )
                setHomeLauncherResultContract.launch(intent)
                func()
            }
        }
        view.findViewById<View>(R.id.try_it_out).apply {
            setOnClickListener { requireActivity().finish() }
        }
    }

}