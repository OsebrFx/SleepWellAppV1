package com.sleepwell.fitness

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.sleepwell.R
import com.sleepwell.databinding.ActivityWorkoutLogBinding
import com.sleepwell.databinding.ItemWorkoutBinding

/**
 * Activity for logging workouts and viewing workout history.
 */
class WorkoutLogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutLogBinding
    private lateinit var repository: WorkoutRepository
    private lateinit var adapter: WorkoutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = WorkoutRepository(this)

        setupUI()
        loadWorkouts()
        updateStats()
    }

    private fun setupUI() {
        binding.btnClose.setOnClickListener {
            finish()
        }

        binding.btnAddWorkout.setOnClickListener {
            showAddWorkoutDialog()
        }

        // Setup RecyclerView
        adapter = WorkoutAdapter(
            onDeleteClick = { workout ->
                deleteWorkout(workout)
            }
        )
        binding.rvWorkoutHistory.layoutManager = LinearLayoutManager(this)
        binding.rvWorkoutHistory.adapter = adapter
    }

    private fun showAddWorkoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_workout, null)
        val spinnerType = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerWorkoutType)
        val etDuration = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDuration)
        val etNotes = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNotes)

        // Setup workout type spinner
        val workoutTypes = WorkoutType.values().map { it.displayName }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, workoutTypes)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = spinnerAdapter

        MaterialAlertDialogBuilder(this)
            .setTitle("Ajouter un entraînement")
            .setView(dialogView)
            .setPositiveButton("Ajouter") { _, _ ->
                val selectedType = WorkoutType.values()[spinnerType.selectedItemPosition]
                val duration = etDuration.text.toString().toIntOrNull() ?: 0
                val notes = etNotes.text.toString()

                if (duration > 0) {
                    val calories = (duration * selectedType.caloriesPerMinute).toInt()
                    val workout = Workout(
                        type = selectedType,
                        durationMinutes = duration,
                        caloriesBurned = calories,
                        notes = notes
                    )
                    repository.saveWorkout(workout)
                    loadWorkouts()
                    updateStats()
                    Snackbar.make(
                        binding.root,
                        "Entraînement ajouté",
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    Snackbar.make(
                        binding.root,
                        "Veuillez entrer une durée valide",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun loadWorkouts() {
        val workouts = repository.getAllWorkouts()
        adapter.submitList(workouts)

        if (workouts.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvWorkoutHistory.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvWorkoutHistory.visibility = View.VISIBLE
        }
    }

    private fun updateStats() {
        val stats = repository.getStats(7)
        binding.tvTotalWorkouts.text = stats.totalWorkouts.toString()
        binding.tvTotalDuration.text = stats.getFormattedDuration()
        binding.tvTotalCalories.text = stats.totalCalories.toString()
    }

    private fun deleteWorkout(workout: Workout) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Supprimer l'entraînement")
            .setMessage("Voulez-vous vraiment supprimer cet entraînement ?")
            .setPositiveButton("Supprimer") { _, _ ->
                repository.deleteWorkout(workout.id)
                loadWorkouts()
                updateStats()
                Snackbar.make(
                    binding.root,
                    "Entraînement supprimé",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
}

/**
 * RecyclerView adapter for displaying workout items.
 */
class WorkoutAdapter(
    private val onDeleteClick: (Workout) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    private var workouts = listOf<Workout>()

    fun submitList(newWorkouts: List<Workout>) {
        workouts = newWorkouts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemWorkoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WorkoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(workouts[position])
    }

    override fun getItemCount() = workouts.size

    inner class WorkoutViewHolder(
        private val binding: ItemWorkoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(workout: Workout) {
            binding.tvWorkoutType.text = workout.type.displayName
            binding.tvWorkoutDate.text = workout.getFormattedDate()
            binding.tvWorkoutDuration.text = workout.getFormattedDuration()
            binding.tvWorkoutCalories.text = "${workout.caloriesBurned} cal"

            if (workout.notes.isNotEmpty()) {
                binding.tvWorkoutNotes.text = workout.notes
                binding.tvWorkoutNotes.visibility = View.VISIBLE
            } else {
                binding.tvWorkoutNotes.visibility = View.GONE
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(workout)
            }
        }
    }
}
