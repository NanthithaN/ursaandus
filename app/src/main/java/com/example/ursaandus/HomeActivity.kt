package com.example.ursaandus
import android.view.Menu
import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val username = intent.getStringExtra("username")
        val welcomeText = findViewById<TextView>(R.id.tvGreeting)

        welcomeText.text = "Hi $username! What's brewing in your honey pot of thoughts?"

        // ✅ Register Context Menu
        registerForContextMenu(welcomeText)
    }

    // ✅ Create Context Menu
    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.home_context_menu, menu)
    }

    // ✅ Handle Menu Clicks
    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.menu_profile -> {
                Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.menu_settings -> {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.menu_logout -> {
                // 🔁 Logout → back to Register Page
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
                finish()
                true
            }

            else -> super.onContextItemSelected(item)
        }

    }
    // ✅ Create Options Menu (3-dot menu)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_options_menu, menu)
        return true
    }

    // ✅ Handle Options Menu Clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.opt_profile -> {
                Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.opt_settings -> {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.opt_logout -> {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}
