extends Node

# LevelManager.gd
# Manages level progression and difficulty configurations in multiples of 5.

signal level_changed(new_level: int)
signal level_progress_updated(current: int, total: int)

var current_level: int = 1
var questions_completed_in_level: int = 0
var total_questions_per_level: int = 5

# Difficulty Configuration dictionary per tier (multiples of 5)
var difficulty_tiers: Dictionary = {
	1: { # Level 1-5: Easy Addition & Subtraction (1-10)
		"name": "EASY (Tier 1)",
		"operations": ["+"],
		"min_num": 1,
		"max_num": 10,
		"target_questions": 5
	},
	2: { # Level 6-10: Easy Addition & Subtraction (1-20)
		"name": "EASY (Tier 2)",
		"operations": ["+", "-"],
		"min_num": 1,
		"max_num": 20,
		"target_questions": 5
	},
	3: { # Level 11-15: Medium (Addition, Subtraction & Intro Multiplication)
		"name": "MEDIUM (Tier 3)",
		"operations": ["+", "-", "×"],
		"min_num": 2,
		"max_num": 30,
		"target_questions": 5
	},
	4: { # Level 16-20: Hard (Full Multiplication & Division tables)
		"name": "HARD (Tier 4)",
		"operations": ["×", "÷"],
		"min_num": 2,
		"max_num": 12,
		"target_questions": 5
	},
	5: { # Level 21+: Expert (All 4 Operations, large numbers)
		"name": "EXPERT (Tier 5)",
		"operations": ["+", "-", "×", "÷"],
		"min_num": 2,
		"max_num": 50,
		"target_questions": 6
	}
}

func _ready() -> void:
	reset_level_progress()

func get_current_tier() -> int:
	var tier = int(ceil(float(current_level) / 5.0))
	return clampi(tier, 1, 5)

func get_level_config(level_num: int = -1) -> Dictionary:
	var lvl = current_level if level_num == -1 else level_num
	var tier = int(ceil(float(lvl) / 5.0))
	tier = clampi(tier, 1, 5)
	return difficulty_tiers[tier]

func record_question_solved() -> bool:
	questions_completed_in_level += 1
	level_progress_updated.emit(questions_completed_in_level, total_questions_per_level)
	if questions_completed_in_level >= total_questions_per_level:
		return true # Level complete!
	return false

func get_progress_ratio() -> float:
	if total_questions_per_level == 0:
		return 0.0
	return clampf(float(questions_completed_in_level) / float(total_questions_per_level), 0.0, 1.0)

func advance_level() -> void:
	current_level += 1
	reset_level_progress()
	level_changed.emit(current_level)

func reset_level_progress() -> void:
	questions_completed_in_level = 0
	var config = get_level_config()
	total_questions_per_level = config.get("target_questions", 5)
	level_progress_updated.emit(questions_completed_in_level, total_questions_per_level)
