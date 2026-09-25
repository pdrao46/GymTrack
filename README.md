# 🏋️ GymTrack

**GymTrack** é um aplicativo Android completo para **organização, acompanhamento e gerenciamento de treinos de academia** — 100% offline, em português (pt-BR), sem login, sem anúncios e sem envio de dados para servidores.

---

## ✨ Funcionalidades

| Área | Recursos |
|---|---|
| 🏠 **Início (Dashboard)** | Saudação, treino do dia com progresso (% e séries), botão "COMEÇAR TREINO", próximo treino, sequência de dias treinando 🔥, resumo da semana, insights inteligentes |
| 📅 **Agenda** | Visão semanal (Seg–Dom) com status: 🟢 concluído, 🔵 planejado, 🟠 não realizado, ⚪ descanso; calendário mensal de frequência com treinos/faltas/descansos |
| 💪 **Treinos** | Criar/editar/duplicar/excluir treinos, dia da semana, horário, tempo estimado (com cálculo automático), cor, observações, exercícios ordenáveis com séries/reps/carga/descanso/RIR/RPE/tempo/método |
| 📚 **Banco de exercícios** | 60+ exercícios em 11 grupos musculares com instruções, dicas, erros comuns, músculos secundários, equipamento, observação pessoal, favoritos e criação de exercícios personalizados |
| ▶️ **Modo Treino Agora** | Um exercício por vez, séries com "CONCLUIR SÉRIE", +série, alterar carga (+1/+2,5/+5), observações, pular exercício, avanço automático, pausar/retomar, progresso ao vivo |
| ⏱️ **Cronômetro de descanso** | Inicia automaticamente ao concluir série; 30/45/60/90/120s ou padrão do exercício; ±15s, pausar, pular; som + vibração + notificação ao terminar |
| 📈 **Progresso** | Estatísticas gerais (treinos, tempo, média, séries, reps, volume), gráficos de volume e treinos por semana, evolução por exercício com gráfico de carga, sistema inteligente com insights |
| 🏆 **Recordes** | PRs automáticos por exercício (melhor carga + reps + data) |
| 🗓️ **Histórico** | Todos os treinos realizados com detalhes por série, filtros, exclusão e "repetir treino" |
| 🎯 **Metas** | Frequência semanal, total de treinos, sequência de dias, volume semanal e carga de exercício — progresso automático |
| 📏 **Medidas** | Peso, braço, peito, cintura, quadril, coxa e panturrilha com gráficos de evolução e unidade kg/lb |
| 📷 **Fotos de evolução** | Galeria privada (armazenamento interno do app) com data e observação |
| 📝 **Anotações** | Bloco de notas com busca por palavra e favoritos |
| 💡 **Dicas** | 30+ dicas em 8 categorias (técnica, progressão, descanso, organização, recuperação, treino, alimentação, consistência) |
| ✅ **Checklists** | Pré-treino e pós-treino personalizáveis |
| 🔔 **Lembretes** | Notificações por dia da semana e horário (sobrevivem à reinicialização do aparelho) |
| ⭐ **Favoritos e busca global** | Favoritos de treinos/exercícios/dicas/anotações; busca por exercício, treino, data, grupo muscular e observações |
| 🧩 **Modelos prontos** | Full Body, Upper/Lower, Push/Pull/Legs, ABC, 4 e 5 dias — todos editáveis |
| ⚙️ **Personalização** | Nome do app, tema claro/escuro/sistema, unidade kg/lb, primeiro dia da semana, estilo dos cards, som/vibração/notificações |
| 💾 **Backup** | Exportar/importar JSON (backup completo) e exportar CSV (planilha) via compartilhamento do Android |
| 🔒 **Privacidade** | Tudo local no aparelho; sem cadastro; seção "Privacidade e dados" com opção de apagar tudo |

---

## 📱 Instalar o APK

1. Baixe o arquivo **`apk/GymTrack.apk`** (ou o asset da release **`apk-latest`** no GitHub).
2. No Android, toque no arquivo e permita "Instalar apps desconhecidos" se solicitado.
3. Pronto — o app funciona totalmente offline.

> O APK é assinado em modo release pelo pipeline de CI.

## 🛠️ Compilar do código-fonte

Requisitos: **Android Studio** (ou JDK 17 + Android SDK 35) e internet para baixar dependências.

```bash
# Linux/macOS
./gradlew assembleDebug     # APK de teste em app/build/outputs/apk/debug/
./gradlew assembleRelease -DGT_STORE_FILE=/caminho/keystore/gymtrack.jks
```

- Sem keystore local, use `assembleDebug` para testar.
- Para release, gere um keystore próprio:
  `keytool -genkeypair -keystore gymtrack.jks -alias gymtrack -keyalg RSA -keysize 2048 -validity 10950`

## 🤖 CI (GitHub Actions)

O arquivo [`ci/build-apk.yml`](ci/build-apk.yml) contém o pipeline que compila o APK release, publica a release `apk-latest` e faz commit do APK na branch. Para ativá-lo, copie/mova o arquivo para `.github/workflows/build-apk.yml` no GitHub (a interface web do GitHub permite criar o arquivo com o mesmo conteúdo).

## 🧱 Stack técnica

- **Kotlin 2.0** + **Jetpack Compose** (Material 3)
- **SQLite** local (sem dependência de servidor), **DataStore** para configurações
- **AlarmManager + BroadcastReceivers** para lembretes; **Photo Picker** para fotos
- minSdk 26 (Android 8.0+) · targetSdk 35 (Android 15)

## 📁 Estrutura

```
app/src/main/java/com/gymtrack/app/
├── GymTrackApp.kt        # Application (canais de notificação, init)
├── MainActivity.kt       # Activity única (Compose)
├── data/                 # Core (modelos, Graph), Db, Repo, Seed,
│                         # Stats, Backup, Settings
├── rem/                  # Lembretes (AlarmManager, receivers, som/vibração)
├── ui/
│   ├── theme/            # Tema claro/escuro
│   ├── Components.kt     # Cards, gráficos, diálogos, campos
│   ├── nav/Nav.kt        # Navegação (5 abas + telas)
│   └── screens/          # Todas as telas (Início, Agenda, Treinos,
│                         # Treino Agora, Histórico, Progresso, etc.)
└── res/                  # Ícones adaptativos, strings, temas
```

## ⚠️ Aviso

Os modelos de treino e as dicas são **conteúdo educativo/genérico** e não substituem a orientação de profissionais de educação física e saúde.

---

GymTrack v1.0.0 · feito para treinar com foco. 💪
