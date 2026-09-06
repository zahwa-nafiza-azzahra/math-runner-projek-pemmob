extends Node

# ScoreManager.gd
# Tracks score, coins, combo multiplier, accuracy, and lives (health).

signal score_changed(new_score: int)
signal coins_changed(new_coins: int)
signal combo_changed(new_combo: int)
signal lives_changed(new_lives: int)

var score: int = 0
var best_score: int = 1250
var coins: int = 0
var combo: int = 1
var max_lives: int = 3
var current_lives: int = 3
var total_correct: int = 0
var total_attempted: int = 0

func _ready() -> void:
	reset_game_stats()

func reset_game_stats() -> void:
	score = 0
	coins = 0
	combo = 1
	current_lives = max_lives
	total_correct = 0
	total_attempted = 0
	emit_all_stats()

func add_correct_answer_score() -> int:
	total_attempted += 1
	total_correct += 1
	var points = 100 * combo
	score += points
	coins += 5
	combo += 1
	if score > best_score:
		best_score = score
	score_changed.emit(score)
	coins_changed.emit(coins)
	combo_changed.emit(combo)
	return points

func add_coin(amount: int = 1) -> void:
	coins += amount
	score += amount * 10
	score_changed.emit(score)
	coins_changed.emit(coins)

func apply_wrong_answer_penalty() -> void:
	total_attempted += 1
	combo = 1
	current_lives = clampi(current_lives - 1, 0, max_lives)
	combo_changed.emit(combo)
	lives_changed.emit(current_lives)

func get_accuracy_percent() -> int:
	if total_attempted == 0:
		return 100
	return int((float(total_correct) / float(total_attempted)) * 100.0)

func emit_all_stats() -> void:
	score_changed.emit(score)
	coins_changed.emit(coins)
	combo_changed.emit(combo)
	lives_changed.emit(current_lives)
