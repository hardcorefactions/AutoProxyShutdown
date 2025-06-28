# AutoProxyShutdown

A lightweight and minimalistic Minecraft proxy plugin that **automatically restarts your proxy when no players are online**. Perfect for routine restarts, memory cleanup, or crash recovery—**without ever kicking players mid-session**.

## 🌟 Why Use AutoProxyShutdown?

Most restart plugins (like `UltimateAutoRestart`) trigger on schedules, even when players are online—causing disconnections and frustration. AutoProxyShutdown solves this by:

- Monitoring player count
- Waiting until the server is completely empty
- Then restarting the proxy automatically

This ensures your server restarts **only when it's safe to do so**, keeping your community happy and your backend healthy.

## ⚙️ Configuration

The plugin has a single, easy-to-use config option:

```yaml
RESTART_AFTER: 10m