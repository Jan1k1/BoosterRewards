
# ⚡ EnchantLimiter Project Guidelines

## 🎯 Project Goals
- Build the **best open-source enchantment limiter plugin** on Modrinth.
- **Zero bypasses**: Strict enforcement.
- **High Performance**: Asynchronous operations where possible to prevent server lag.
- **Support**: 1.20+, Folia, and all server types (Spigot, Paper, Purpur).
- **Usability**: Easy configuration via file or in-game GUI.

## 🚫 "No AI Vibe" Code Rules (Human Coding Standard)
1. **No Over-Engineering**: Avoid complex abstractions for simple logic.
2. **Standard Naming**: Use clear, descriptive variable and method names (no AI-generated weirdness like `utilizeEnchantmentProcessing`).
3. **No Unnecessary Comments**: Code should be self-documenting. Only comment "why", not "what".
4. **Clean Project Structure**: Organize packages logically (e.g., `commands`, `listeners`, `managers`, `gui`).
5. **No "Magic" Code**: Avoid convoluted streams or lambdas if a simple loop is clearer.
6. **Focus on Core Logic**: Prioritize functionality and performance over flashy patterns.
7. **Direct Implementation**: If a feature is simple, keep the implementation simple.

## 🛠️ Technical Requirements
- **Async Handling**: Database/config I/O must be async. Heavy calculations offloaded where safe.
- **GUI System**: Robust inventory GUI for managing limits.
- **Config System**: Clean YAML configuration with reloading capability.
- **Platform Agnostic**: Abstraction layer for NMS/version differences if needed (likely minimal with modern API).

## 📝 User Persona
- **Creator**: Jan1k / Studio Jan1k
- **Role**: Senior Developer / Project Lead
- **Expectation**: Professional, high-quality, production-ready code.

---
*Created by Antigravity for Jan1k*
