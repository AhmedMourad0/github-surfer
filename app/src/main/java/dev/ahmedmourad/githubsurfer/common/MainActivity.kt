package dev.ahmedmourad.githubsurfer.common

import android.app.SearchManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dev.ahmedmourad.githubsurfer.R
import dev.ahmedmourad.githubsurfer.databinding.ActivityMainBinding
import dev.ahmedmourad.githubsurfer.di.injector

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        injector.inject(this)
        overridePendingTransition(0, 0)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent ?: return
        if (Intent.ACTION_SEARCH == intent.action) {
            onSearch(intent.getStringExtra(SearchManager.QUERY))
        }
    }

    private fun onSearch(query: String?) {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment)
        val handler = navHostFragment!!.childFragmentManager
            .fragments.firstOrNull() as? SearchHandler
        handler?.onSearch(query.takeUnless(String?::isNullOrBlank))
    }
}
