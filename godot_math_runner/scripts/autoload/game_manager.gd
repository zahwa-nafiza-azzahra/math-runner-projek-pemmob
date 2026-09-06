extends Node

# GameManager.gd
# Central state coordinator and event bus for Math Runner.

enum GameState {
	RUNNING,            # Player is running along track, collecting coins
	QUESTION_ENCOUNTER, # Obstacle / Gate encounter: Runner stops to solve math
	PAUSED,             # Game paused modal
	LEVEL_COMPLETE,     # Level cleared modal
	GAME_OVER           # Out of lives modal
}

signal state_changed(new_state: GameState, old_state: GameState)
signal request_resume_run()
signal request_stop_for_question()

var current_state: GameState = GameState.RUNNING

func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS

func set_state(new_state: GameState) -> void:
	if current_state == new_state:
		return
	var old_state = current_state
	current_state = new_state
	state_changed.emit(new_state, old_state)

	match new_state:
		GameState.RUNNING:
			get_tree().paused = false
			request_resume_run.emit()
		GameState.QUESTION_ENCOUNTER:
			get_tree().paused = false
			request_stop_for_question.emit()
		GameState.PAUSED, GameState.LEVEL_COMPLETE, GameState.GAME_OVER:
			get_tree().paused = true

func start_new_game() -> void:
	ScoreManager.reset_game_stats()
	LevelManager.current_level = 1
	LevelManager.reset_level_progress()
	set_state(GameState.RUNNING)

func restart_current_level() -> void:
	ScoreManager.current_lives = ScoreManager.max_lives
	ScoreManager.combo = 1
	ScoreManager.emit_all_stats()
	LevelManager.reset_level_progress()
	set_state(GameState.RUNNING)

func advance_to_next_level() -> void:
	LevelManager.advance_level()
	ScoreManager.current_lives = ScoreManager.max_lives
	ScoreManager.emit_all_stats()
	set_state(GameState.RUNNING)

func toggle_pause() -> void:
	if current_state == GameState.RUNNING or current_state == GameState.QUESTION_ENCOUNTER:
		set_state(GameState.PAUSED)
	elif current_state == GameState.PAUSED:
		set_state(GameState.RUNNING)
