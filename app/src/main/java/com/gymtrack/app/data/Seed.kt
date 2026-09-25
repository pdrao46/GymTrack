package com.gymtrack.app.data

import android.content.ContentValues

object Seed {

    val MUSCLE_GROUPS = listOf(
        "Peito", "Costas", "Ombros", "Bíceps", "Tríceps", "Quadríceps",
        "Posterior", "Glúteos", "Panturrilhas", "Abdômen", "Cardio"
    )

    data class Ex(
        val name: String, val group: String, val secondary: String, val equipment: String,
        val instructions: String, val tips: String, val mistakes: String
    )

    private val CATALOG = listOf(
        // ---- PEITO ----
        Ex("Supino reto com barra", "Peito", "Tríceps, Ombros", "Barra", 
            "Deitado no banco, segure a barra na largura dos ombros, desça controlado até o peito e empurre para cima.",
            "Mantenha os pés no chão e as escápulas retraídas.", "Quitar o quadril do banco e descer a barra sem controle."),
        Ex("Supino inclinado com halteres", "Peito", "Ombros, Tríceps", "Halteres",
            "Banco a 30–45°, desça os halteres até a linha do peito e suba juntando levemente.",
            "Não deixe os cotovelos abrirem demais.", "Exagerar na inclinação e virar supino de ombros."),
        Ex("Supino máquina", "Peito", "Tríceps", "Máquina",
            "Ajuste o assento, segure as alças na altura do peito e empurre até estender sem travar os cotovelos.",
            "Ótimo para iniciantes e para treinar com segurança.", "Assento muito baixo ou muito alto."),
        Ex("Crucifixo máquina (voador)", "Peito", "Ombros (anterior)", "Máquina",
            "Sentado, junte as alças à frente do peito em arco controlado e volte devagar.",
            "Sinta o alongamento no início do movimento.", "Usar impulso e juntar os ombros."),
        Ex("Crucifixo com halteres", "Peito", "—", "Halteres",
            "Deitado, braços abertos em leve flexão, junte os halteres descrevendo um arco.",
            "Desça até sentir alongamento sem dor.", "Transformar em supino flexionando demais o cotovelo."),
        Ex("Paralelas", "Peito", "Tríceps, Ombros", "Peso do corpo",
            "Nas paralelas, incline o tronco à frente e desça até 90° de cotovelo, depois empurre.",
            "Inclinar o tronco foca o peito.", "Descer demais e sobrecarregar o ombro."),
        Ex("Crossover na polia", "Peito", "—", "Polia",
            "Em pé entre as polias altas, cruze as mãos à frente do peito e abra controlado.",
            "Pausa de 1s na contração.", "Usar carga alta e perder a amplitude."),
        // ---- COSTAS ----
        Ex("Barra fixa (pull-up)", "Costas", "Bíceps", "Peso do corpo",
            "Pendurado com pegada pronada, puxe até o queixo passar a barra e desça controlado.",
            "Inicie puxando as escápulas para baixo.", "Balançar o corpo (kipping) sem controle."),
        Ex("Puxada frontal na polia", "Costas", "Bíceps", "Polia",
            "Sentado, puxe a barra até o peito superior mantendo o tronco firme.",
            "Pense em levar os cotovelos para baixo e para trás.", "Puxar atrás da nuca e usar muito impulso."),
        Ex("Remada curvada com barra", "Costas", "Bíceps, Posterior", "Barra",
            "Tronco inclinado ~45°, puxe a barra em direção ao abdômen mantendo a coluna neutra.",
            "Coluna sempre neutra e abdômen firme.", "Arredondar as costas e erguer o tronco a cada repetição."),
        Ex("Remada baixa na polia", "Costas", "Bíceps", "Polia",
            "Sentado, puxe o triângulo até o abdômen com cotovelos rentes ao corpo.",
            "Alongue bem as costas na volta.", "Balançar o tronco para ajudar no puxão."),
        Ex("Remada unilateral com halter", "Costas", "Bíceps", "Halter",
            "Apoiado no banco, puxe o halter em direção ao quadril e desça controlado.",
            "Puxe pensando no cotovelo, não na mão.", "Girar o tronco durante o movimento."),
        Ex("Levantamento terra", "Costas", "Posterior, Glúteos, Quadríceps", "Barra",
            "Com a coluna neutra, erga a barra estendendo quadril e joelhos ao mesmo tempo.",
            "Barra rente às pernas do início ao fim.", "Arredondar a lombar e 'rebolar' o peso."),
        Ex("Pulldown com corda (braço reto)", "Costas", "—", "Polia",
            "Em pé, braços estendidos, puxe a corda em direção às coxas sentindo o dorsal.",
            "Mantenha cotovelos quase travados.", "Flexionar os cotovelos e virar tríceps."),
        // ---- OMBROS ----
        Ex("Desenvolvimento com halteres", "Ombros", "Tríceps", "Halteres",
            "Sentado ou em pé, empurre os halteres acima da cabeça e desça até a altura das orelhas.",
            "Não deixe a lombar arquear em excesso.", "Descer muito rápido e bater os halteres."),
        Ex("Desenvolvimento máquina", "Ombros", "Tríceps", "Máquina",
            "Sentado na máquina, empurre as alças acima da cabeça de forma controlada.",
            "Ajuste o assento para as alças ficarem na altura dos ombros.", "Semiarredondar a coluna."),
        Ex("Elevação lateral", "Ombros", "—", "Halteres",
            "Em pé, eleve os braços lateralmente até a linha dos ombros e desça devagar.",
            "Cotovelos levemente flexionados lideram o movimento.", "Usar impulso do tronco e subir além dos ombros."),
        Ex("Elevação frontal", "Ombros", "—", "Halteres",
            "Em pé, eleve os halteres à frente até a altura dos olhos, alternando ou juntos.",
            "Carga leve e controle total.", "Balançar o corpo para jogar o peso."),
        Ex("Crucifixo inverso", "Ombros", "Costas", "Halteres ou máquina",
            "Tronco inclinado, abra os braços lateralmente sentindo a região posterior do ombro.",
            "Pense em 'abrir' os braços, não levantar.", "Carga excessiva e amplitude curta."),
        Ex("Encolhimento de ombros", "Ombros", "—", "Halteres ou barra",
            "Em pé, eleve os ombros em direção às orelhas e pause 1s no topo.",
            "Movimento só dos ombros, braços relaxados.", "Girar os ombros em círculos."),
        // ---- BÍCEPS ----
        Ex("Rosca direta com barra", "Bíceps", "—", "Barra",
            "Em pé, flexione os cotovelos elevando a barra sem balançar o tronco.",
            "Cotovelos fixos junto ao tronco.", "Usar o quadril para impulsionar a barra."),
        Ex("Rosca alternada com halteres", "Bíceps", "—", "Halteres",
            "Em pé, flexione um braço de cada vez com supinação (girar o punho para fora).",
            "Desça devagar contando 2s.", "Encolher os ombros ao subir."),
        Ex("Rosca martelo", "Bíceps", "Antebraço", "Halteres",
            "Pegada neutra (polegar para cima), flexione os cotovelos alternando ou juntos.",
            "Trabalha braquial e antebraço.", "Movimento rápido sem controle."),
        Ex("Rosca no banco Scott", "Bíceps", "—", "Barra ou halteres",
            "Apoie os braços no banco inclinado e faça a rosca sem tirar os cotovelos do apoio.",
            "Amplitude completa sem travar embaixo.", "Levantar os cotovelos do banco."),
        Ex("Rosca na polia baixa", "Bíceps", "—", "Polia",
            "Em frente à polia, flexione os cotovelos puxando a barra até os ombros.",
            "Tensão constante em toda a amplitude.", "Dar passos para trás e usar o corpo."),
        // ---- TRÍCEPS ----
        Ex("Tríceps na polia (corda/barra)", "Tríceps", "—", "Polia",
            "Em pé, estenda os cotovelos empurrando a corda para baixo até travar levemente.",
            "Cotovelos colados ao tronco.", "Abrir os cotovelos e inclinar o corpo."),
        Ex("Tríceps francês (supra)", "Tríceps", "—", "Halter ou barra W",
            "Sentado/deitado, flexione apenas os cotovelos descendo o peso atrás da cabeça e estenda.",
            "Cotovelos apontando para cima e para frente.", "Abrir muito os cotovelos."),
        Ex("Tríceps testa", "Tríceps", "—", "Barra W ou halteres",
            "Deitado, desça o peso em direção à testa flexionando os cotovelos e volte.",
            "Controle total na descida.", "Descer rápido e bater o peso na testa."),
        Ex("Tríceps banco (banco invertido)", "Tríceps", "Peito, Ombros", "Banco",
            "Mãos no banco atrás do corpo, desça flexionando os cotovelos e empurre para subir.",
            "Quadril rente ao banco.", "Descer abaixo do confortável e estressar o ombro."),
        Ex("Tríceps corda sobre a cabeça", "Tríceps", "—", "Polia",
            "De costas para a polia, corda sobre a cabeça, estenda os cotovelos à frente.",
            "Grande alongamento do tríceps.", "Arquear a lombar."),
        // ---- QUADRÍCEPS ----
        Ex("Agachamento livre", "Quadríceps", "Glúteos, Costas, Posterior", "Barra",
            "Barra nas costas, desça até coxas paralelas (ou mais) mantendo coluna neutra e suba.",
            "Joelhos acompanham a ponta dos pés.", "Subir os calcanhares e a lombar arredondar."),
        Ex("Leg press 45°", "Quadríceps", "Glúteos", "Máquina",
            "Sentado na máquina, desça a plataforma até ~90° de joelho e empurre sem travar.",
            "Não deixe o quadril descolar do apoio.", "Descer demais e curvar a lombar."),
        Ex("Hack machine", "Quadríceps", "Glúteos", "Máquina",
            "Ombros sob as almofadas, desça controlado e suba sem travar os joelhos.",
            "Foco em manter o tronco apoiado.", "Amplitude curta com carga alta."),
        Ex("Cadeira extensora", "Quadríceps", "—", "Máquina",
            "Sentado, estenda os joelhos até quase travar e desça controlado.",
            "Pause 1s no topo.", "Usar impulso e soltar o peso na volta."),
        Ex("Afundo (lunge)", "Quadríceps", "Glúteos, Posterior", "Halteres ou peso do corpo",
            "Dê um passo à frente e desça o joelho de trás em direção ao chão, depois suba.",
            "Tronco ereto, passo suficiente.", "Joelho da frente passar muito da ponta do pé com tronco caído."),
        Ex("Agachamento búlgaro", "Quadríceps", "Glúteos, Posterior", "Halteres",
            "Pé de trás no banco, desça na perna da frente até ~90° e suba.",
            "Distância do banco ajustada ao seu conforto.", "Perder o equilíbrio por passos muito curtos."),
        Ex("Agachamento na máquina (smith)", "Quadríceps", "Glúteos", "Máquina",
            "Mesmo padrão do agachamento livre, com trajetória guiada pela barra do smith.",
            "Boa opção para treinar o padrão com segurança.", "Pés muito à frente ou muito atrás."),
        // ---- POSTERIOR ----
        Ex("Mesa flexora", "Posterior", "Panturrilhas", "Máquina",
            "De bruços, flexione os joelhos levando os calcanhares ao glúteo e desça controlado.",
            "Quadril colado no apoio.", "Erguer o quadril para completar a repetição."),
        Ex("Cadeira flexora", "Posterior", "—", "Máquina",
            "Sentado, flexione os joelhos empurrando as almofadas para baixo e volte devagar.",
            "Ajuste o encosto ao seu tamanho.", "Estender tudo de uma vez na volta."),
        Ex("Stiff com barra", "Posterior", "Glúteos, Lombar", "Barra",
            "Pernas quase estendidas, desça a barra deslizando nas coxas sentindo alongar o posterior.",
            "Coluna neutra e movimento vindo do quadril.", "Transformar em terra com joelhos flexionando muito."),
        Ex("Mesa adaptada (terra romeno)", "Posterior", "Glúteos", "Halteres",
            "Mesmo padrão do stiff com halteres, foco no alongamento do posterior.",
            "Desça apenas até seu limite de alongamento.", "Arredondar as costas no fim da descida."),
        // ---- GLÚTEOS ----
        Ex("Elevação pélvica (hip thrust)", "Glúteos", "Posterior, Quadríceps", "Barra ou banco",
            "Costas apoiadas no banco, empurre o quadril para cima contraindo o glúteo e pause no topo.",
            "Queixo no peito e costelas para baixo.", "Hiperextender a lombar no topo."),
        Ex("Coixa na polia (abdução)", "Glúteos", "—", "Polia ou máquina",
            "Afaste a perna contra a resistência e volte controlado.",
            "Tronco estável, sem inclinar.", "Usar impulso e carga alta demais."),
        Ex("Glúteo 4 apoios (kickback)", "Glúteos", "Posterior", "Polia, caneleira ou peso do corpo",
            "Em 4 apoios ou em pé, estenda o quadril levando o pé para trás e para cima.",
            "Movimento curto e contração forte.", "Arquear a lombar para subir mais."),
        // ---- PANTURRILHAS ----
        Ex("Panturrilha em pé", "Panturrilhas", "—", "Máquina ou degrau",
            "Na ponta dos pés, suba ao máximo, pause 1s e desça alongando.",
            "Amplitude completa: alonga embaixo, contrai em cima.", "Repetições rápidas e curtas."),
        Ex("Panturrilha sentado", "Panturrilhas", "—", "Máquina",
            "Sentado, eleve os calcanhares contra a almofada e desça controlado.",
            "Foca o sóleo (parte profunda).", "Tirar o calcanhar do apoio na descida."),
        Ex("Panturrilha no leg press", "Panturrilhas", "—", "Máquina",
            "Ponta dos pés na plataforma, empurre e desça alongando bem.",
            "Não travar e soltar o peso.", "Amplitude muito curta."),
        // ---- ABDÔMEN ----
        Ex("Abdominal supra (elevação de pernas)", "Abdômen", "—", "Banco ou solo",
            "Deitado, eleve as pernas flexionando o quadril e desça controlado sem tocar o chão.",
            "Lombar apoiada, movimento controlado.", "Usar impulso e arquear a lombar."),
        Ex("Abdominal infra (canivete)", "Abdômen", "—", "Solo ou banco",
            "Junte cotovelos e joelhos contraindo o abdômen e volte alongando.",
            "Expire na contração.", "Puxar a nuca com as mãos."),
        Ex("Prancha isométrica", "Abdômen", "Ombros, Glúteos", "Peso do corpo",
            "Antebraços no chão, corpo alinhado, contraia abdômen e glúteo e mantenha.",
            "Quadril alinhado, sem cair ou subir demais.", "Segurar a respiração."),
        Ex("Abdominal na polia alta", "Abdômen", "—", "Polia",
            "Ajoelhado, segure a corda atrás da cabeça e flexione o tronco em direção ao quadril.",
            "Movimento de 'enrolar' a coluna.", "Puxar com os braços em vez de flexionar o tronco."),
        // ---- CARDIO ----
        Ex("Esteira (caminhada/corrida)", "Cardio", "Quadríceps, Panturrilhas", "Máquina",
            "Caminhe ou corra em ritmo constante ou em intervalos conforme seu plano.",
            "Pace conversável para zona 2.", "Segurar no apoio o tempo todo."),
        Ex("Bicicleta ergométrica", "Cardio", "Quadríceps", "Máquina",
            "Pedale em ritmo constante ajustando a resistência conforme o objetivo.",
            "Ajuste a altura do selim.", "Selim baixo demais sobrecarregando o joelho."),
        Ex("Elíptico", "Cardio", "—", "Máquina",
            "Movimento contínuo de braços e pernas em impacto baixo.",
            "Boa opção de cardio de baixo impacto.", "Amplitude curta demais."),
        Ex("Pular corda", "Cardio", "Panturrilhas, Ombros", "Corda",
            "Salte baixo com os punhos girando a corda em ritmo constante.",
            "Saltos curtos e rápidos.", "Saltar alto demais e cansar rápido."),
        Ex("Escada (step machine)", "Cardio", "Glúteos, Quadríceps", "Máquina",
            "Suba degraus em ritmo constante mantendo o tronco ereto.",
            "Não se apoie com todo o peso nos corrimãos.", "Passos apoiados demais nos braços."),
        Ex("Remo ergômetro", "Cardio", "Costas, Quadríceps", "Máquina",
            "Sequência pernas → tronco → braços; retorno na ordem inversa.",
            "Puxe com as pernas primeiro.", "Puxar só com os braços.")
    )

    fun seedExercises(insert: (ContentValues) -> Any) {
        for (e in CATALOG) {
            insert(GymDb.cv(
                "name" to e.name, "muscleGroup" to e.group, "secondary" to e.secondary,
                "equipment" to e.equipment, "instructions" to e.instructions,
                "tips" to e.tips, "mistakes" to e.mistakes,
                "personalNote" to "", "isCustom" to false, "isFavorite" to false
            ))
        }
    }

    // ===== Dicas =====

    data class TipDef(val category: String, val title: String, val content: String)

    val TIP_CATEGORIES = listOf(
        "Técnica", "Progressão", "Descanso", "Organização", "Recuperação", "Treino", "Alimentação geral", "Consistência"
    )

    private val TIPS = listOf(
        TipDef("Técnica", "Amplitude antes de carga", "Prefira fazer o movimento completo com menos peso a fazer meias repetições com mais carga. Amplitude completa constrói mais músculo e protege as articulações."),
        TipDef("Técnica", "Controle a fase excêntrica", "A descida do peso (fase excêntrica) é onde muita mágica acontece. Conte 2 segundos descendo e evite 'soltar' o peso."),
        TipDef("Técnica", "Firme o core", "Antes de exercícios como agachamento e terra, contraia o abdômen como se fosse levar um tapa. Isso protege a coluna."),
        TipDef("Técnica", "Filme-se executando", "Gravar uma série de lado ajuda a ver erros que você não sente. Compare com os vídeos do exercício no banco de exercícios."),
        TipDef("Progressão", "Sobrecarga progressiva simples", "Tente melhorar 1 coisa por treino: 1 repetição a mais, 1kg a mais ou 1 série melhor executada. Progressão pequena e constante vence saltos aleatórios."),
        TipDef("Progressão", "Dupla progressão", "Escolha uma faixa (ex.: 8–12 repetições). Comece no mínimo da faixa e vá somando repetições por semana; ao atingir o máximo, aumente a carga e volte ao mínimo."),
        TipDef("Progressão", "Use o RIR a seu favor", "Deixar 1–3 repetições em reserva (RIR) na maioria das séries mantém a técnica limpa. Reserve RIR 0–1 para a última série quando estiver bem recuperado."),
        TipDef("Progressão", "Anote tudo", "Sem registro não há progressão confiável. Use o modo 'Treino agora' para registrar cargas e repetições — no próximo treino você bate o número anterior."),
        TipDef("Descanso", "Descanso por objetivo", "Força: 2–5 min entre séries. Hipertrofia: 60–120s. Isolados e abdômen: 30–60s. Use o cronômetro do app para não descansar de menos."),
        TipDef("Descanso", "Não tenha pressa nos básicos", "Agachamento, terra e supino pesado pedem mais descanso. Encurtar o descanso nos grandes exercícios derruba o desempenho das séries seguintes."),
        TipDef("Descanso", "Descanso ativo entre exercícios", "Entre exercícios diferentes, alongar levemente o grupo treinado e beber água ajuda sem atrapalhar a próxima série."),
        TipDef("Organização", "Planeje a semana no domingo", "Reserve 5 minutos para conferir se a agenda da semana está com os treinos atribuídos aos dias certos. Ajuste antes, não durante."),
        TipDef("Organização", "Treino curto ainda é treino", "Se o dia apertar, faça a versão mínima: 2 exercícios principais. Manter o hábito vale mais do que a sessão perfeita que não aconteceu."),
        TipDef("Organização", "Separe os treinos por padrão", "Divisões como Upper/Lower ou Push/Pull/Legs ajudam a equilibrar volume entre os grupos e evitam treinar o mesmo músculo em dias seguidos."),
        TipDef("Organização", "Checklist na mochila", "Monte um checklist pré-treino (água, fone, toalha) no app. Menos decisões na hora de sair de casa = menos desculpas."),
        TipDef("Recuperação", "Durma 7–9 horas", "O sono é o suplemento mais potente que existe. Dormir mal derruba força, apetite e humor — e faz faltar na academia."),
        TipDef("Recuperação", "Dor muscular x dor de lesão", "Dor tardia (DMAT) é normal 24–48h depois. Dor pontual, em um lado só ou que piora com movimento específico merece atenção de um profissional."),
        TipDef("Recuperação", "Deload a cada 6–10 semanas", "Uma semana com carga ou volume reduzido (~50–60%) ajuda a recuperar e voltar progredindo. Marque no calendário do app."),
        TipDef("Recuperação", "Hidrate-se", "Desidratação de apenas 2% já reduz o desempenho. Tenha sempre a garrafa à mão e use o checklist pré-treino para lembrar."),
        TipDef("Treino", "Aqueça de verdade", "5–10 minutos de cardio leve + 1–2 séries leves do primeiro exercício preparam articulações e melhoram o desempenho das séries pesadas."),
        TipDef("Treino", "Ordem importa", "Comece pelos exercícios que usam mais músculos (básicos) e deixe isoladores para o fim. Energia no começo do treino é maior."),
        TipDef("Treino", "Cadência do treino", "Sem distração entre séries: cronômetro rodando e celular só para registrar. Treino focado termina em 60–75 minutos com mais qualidade."),
        TipDef("Treino", "Zona 2 é sua amiga", "Cardio leve, em que você consegue conversar, melhora a recuperação e a saúde cardiovascular sem rouba ganhos."),
        TipDef("Alimentação geral", "Proteína em todas as refeições", "Distribuir proteína ao longo do dia (cerca de 0,2–0,4 g/kg por refeição) ajuda na recuperação muscular. Procure um nutricionista para um plano individual."),
        TipDef("Alimentação geral", "Pré-treino simples", "Uma refeição com carboidrato e proteína 1–2 horas antes do treino costuma bastar. Evite experimentar comida nova no dia do treino pesado."),
        TipDef("Alimentação geral", "Não treine 'morto'", "Fome extrema, dor de cabeça ou noites mal dormidas em sequência: adapte o treino (carga e volume menores) em vez de pular por completo."),
        TipDef("Alimentação geral", "Todo dia um pouco de fruta e verdura", "Micronutrientes e fibras sustentam a recuperação e o intestino. Simples e barato: pelo menos uma porção em cada refeição principal."),
        TipDef("Consistência", "Frequência é o segredo", "3 treinos por semana durante 1 ano valem mais do que 6 por semana durante 1 mês. Metas de frequência no app ajudam a manter o ritmo."),
        TipDef("Consistência", "Não quebre a corrente", "O objetivo diário é não deixar passar dois dias seguidos sem treinar. Uma corrente de dias treinados é um ótimo motivador."),
        TipDef("Consistência", "Planos mudam, hábito não", "Viajou, máquina ocupada, imprevisto? Faça a versão alternativa do treino. O hábito é treinado tanto quanto o músculo."),
        TipDef("Consistência", "Comemore PRs", "Cada recorde pessoal registrado é uma prova de progresso. Olhe a aba 'Recordes' quando a motivação estiver baixa.")
    )

    fun seedTips(insert: (ContentValues) -> Any) {
        for (t in TIPS) {
            insert(GymDb.cv(
                "category" to t.category, "title" to t.title, "content" to t.content, "isFavorite" to false
            ))
        }
    }

    fun seedChecklist(insert: (ContentValues) -> Any) {
        val pre = listOf("Garrafa de água", "Fone", "Toalha", "Treino conferido", "Equipamento necessário")
        val post = listOf("Alongar / mobilidade", "Registrar cargas", "Anotar sensações", "Hidratar-se")
        pre.forEachIndexed { i, label ->
            insert(GymDb.cv("category" to "pre", "label" to label, "checked" to false, "orderIndex" to i))
        }
        post.forEachIndexed { i, label ->
            insert(GymDb.cv("category" to "post", "label" to label, "checked" to false, "orderIndex" to i))
        }
    }

    // ===== Modelos prontos de treino =====

    data class ExSpec(val name: String, val sets: Int, val repsMin: Int, val repsMax: Int, val weight: Double, val rest: Int)
    data class DaySpec(val name: String, val dayOfWeek: Int?, val time: String, val exercises: List<ExSpec>)
    data class TemplateDef(val id: String, val title: String, val subtitle: String, val days: List<DaySpec>)

    private fun ex(name: String, sets: Int = 3, rmin: Int = 8, rmax: Int = 12, w: Double = 0.0, rest: Int = 90) =
        ExSpec(name, sets, rmin, rmax, w, rest)

    val TEMPLATES = listOf(
        TemplateDef("fullbody2", "Full Body — 2 dias", "Corpo inteiro, ideal para começar", listOf(
            DaySpec("FULL BODY A", 1, "07:00", listOf(
                ex("Agachamento livre", 3, 8, 12, 0.0, 120), ex("Supino reto com barra", 3, 8, 12, 0.0, 120),
                ex("Remada curvada com barra", 3, 8, 12, 0.0, 120), ex("Elevação lateral", 2, 12, 15, 0.0, 60),
                ex("Abdominal infra (canivete)", 3, 12, 15, 0.0, 45)
            )),
            DaySpec("FULL BODY B", 4, "07:00", listOf(
                ex("Leg press 45°", 3, 10, 12, 0.0, 120), ex("Supino inclinado com halteres", 3, 8, 12, 0.0, 120),
                ex("Puxada frontal na polia", 3, 8, 12, 0.0, 120), ex("Rosca direta com barra", 2, 10, 12, 0.0, 60),
                ex("Tríceps na polia (corda/barra)", 2, 10, 12, 0.0, 60), ex("Prancha isométrica", 3, 30, 45, 0.0, 45)
            ))
        )),
        TemplateDef("upperlower4", "Upper/Lower — 4 dias", "Superiores e inferiores alternados", listOf(
            DaySpec("SUPERIORES A", 1, "07:00", listOf(
                ex("Supino reto com barra", 4, 6, 10, 0.0, 120), ex("Remada curvada com barra", 4, 6, 10, 0.0, 120),
                ex("Desenvolvimento com halteres", 3, 8, 12, 0.0, 90), ex("Puxada frontal na polia", 3, 8, 12, 0.0, 90),
                ex("Elevação lateral", 3, 12, 15, 0.0, 60), ex("Tríceps na polia (corda/barra)", 3, 10, 12, 0.0, 60),
                ex("Rosca martelo", 3, 10, 12, 0.0, 60)
            )),
            DaySpec("INFERIORES A", 2, "07:00", listOf(
                ex("Agachamento livre", 4, 6, 10, 0.0, 150), ex("Leg press 45°", 3, 8, 12, 0.0, 120),
                ex("Mesa flexora", 3, 10, 12, 0.0, 90), ex("Elevação pélvica (hip thrust)", 3, 8, 12, 0.0, 90),
                ex("Panturrilha em pé", 4, 12, 15, 0.0, 60), ex("Prancha isométrica", 3, 30, 45, 0.0, 45)
            )),
            DaySpec("SUPERIORES B", 4, "07:00", listOf(
                ex("Supino inclinado com halteres", 4, 8, 12, 0.0, 120), ex("Remada unilateral com halter", 4, 8, 12, 0.0, 90),
                ex("Desenvolvimento máquina", 3, 8, 12, 0.0, 90), ex("Crossover na polia", 3, 10, 15, 0.0, 60),
                ex("Rosca no banco Scott", 3, 10, 12, 0.0, 60), ex("Tríceps testa", 3, 10, 12, 0.0, 60)
            )),
            DaySpec("INFERIORES B", 5, "07:00", listOf(
                ex("Hack machine", 4, 8, 12, 0.0, 150), ex("Stiff com barra", 3, 8, 12, 0.0, 120),
                ex("Cadeira extensora", 3, 12, 15, 0.0, 60), ex("Afundo (lunge)", 3, 10, 12, 0.0, 90),
                ex("Panturrilha sentado", 4, 15, 20, 0.0, 45), ex("Abdominal na polia alta", 3, 12, 15, 0.0, 45)
            ))
        )),
        TemplateDef("ppl3", "Push/Pull/Legs — 3 dias", "Empurrar, puxar e pernas", listOf(
            DaySpec("PUSH (EMPURRAR)", 1, "07:00", listOf(
                ex("Supino reto com barra", 4, 6, 10, 0.0, 120), ex("Supino inclinado com halteres", 3, 8, 12, 0.0, 90),
                ex("Desenvolvimento com halteres", 3, 8, 12, 0.0, 90), ex("Elevação lateral", 3, 12, 15, 0.0, 60),
                ex("Tríceps na polia (corda/barra)", 3, 10, 12, 0.0, 60), ex("Tríceps francês (supra)", 3, 10, 12, 0.0, 60)
            )),
            DaySpec("PULL (PUXAR)", 3, "07:00", listOf(
                ex("Barra fixa (pull-up)", 4, 6, 10, 0.0, 120), ex("Remada baixa na polia", 3, 8, 12, 0.0, 90),
                ex("Puxada frontal na polia", 3, 8, 12, 0.0, 90), ex("Crucifixo inverso", 3, 12, 15, 0.0, 60),
                ex("Rosca direta com barra", 3, 8, 12, 0.0, 60), ex("Rosca martelo", 3, 10, 12, 0.0, 60)
            )),
            DaySpec("LEGS (PERNAS)", 5, "07:00", listOf(
                ex("Agachamento livre", 4, 6, 10, 0.0, 150), ex("Leg press 45°", 3, 8, 12, 0.0, 120),
                ex("Mesa flexora", 3, 10, 12, 0.0, 90), ex("Cadeira extensora", 3, 12, 15, 0.0, 60),
                ex("Panturrilha em pé", 4, 12, 15, 0.0, 60), ex("Abdominal supra (elevação de pernas)", 3, 12, 15, 0.0, 45)
            ))
        )),
        TemplateDef("abc", "ABC — 3 dias", "Peito+Tríceps / Costas+Bíceps / Pernas", listOf(
            DaySpec("TREINO A — PEITO + TRÍCEPS", 1, "19:00", listOf(
                ex("Supino máquina", 4, 8, 12, 40.0, 90), ex("Supino inclinado com halteres", 3, 8, 12, 0.0, 90),
                ex("Crucifixo máquina (voador)", 3, 10, 15, 0.0, 60), ex("Tríceps na polia (corda/barra)", 3, 10, 12, 0.0, 60),
                ex("Tríceps francês (supra)", 3, 10, 12, 0.0, 60)
            )),
            DaySpec("TREINO B — COSTAS + BÍCEPS", 3, "19:00", listOf(
                ex("Puxada frontal na polia", 4, 8, 12, 0.0, 90), ex("Remada baixa na polia", 3, 8, 12, 0.0, 90),
                ex("Remada unilateral com halter", 3, 10, 12, 0.0, 60), ex("Rosca direta com barra", 3, 8, 12, 0.0, 60),
                ex("Rosca martelo", 3, 10, 12, 0.0, 60)
            )),
            DaySpec("TREINO C — PERNAS", 5, "19:00", listOf(
                ex("Leg press 45°", 4, 8, 12, 120.0, 120), ex("Hack machine", 3, 8, 12, 0.0, 120),
                ex("Mesa flexora", 3, 10, 15, 0.0, 60), ex("Cadeira extensora", 3, 12, 15, 0.0, 60),
                ex("Panturrilha em pé", 4, 12, 20, 0.0, 45)
            ))
        )),
        TemplateDef("4dias", "Treino de 4 dias", "Duas sessões alternadas na semana", listOf(
            DaySpec("TREINO A — CORPO INTEIRO 1", 1, "07:00", listOf(
                ex("Agachamento na máquina (smith)", 4, 8, 12, 0.0, 120), ex("Supino máquina", 4, 8, 12, 0.0, 90),
                ex("Remada baixa na polia", 4, 8, 12, 0.0, 90), ex("Elevação lateral", 3, 12, 15, 0.0, 60),
                ex("Rosca alternada com halteres", 2, 10, 12, 0.0, 60), ex("Tríceps banco (banco invertido)", 2, 10, 12, 0.0, 60)
            )),
            DaySpec("TREINO B — CORPO INTEIRO 2", 4, "07:00", listOf(
                ex("Leg press 45°", 4, 10, 15, 0.0, 120), ex("Desenvolvimento máquina", 3, 8, 12, 0.0, 90),
                ex("Puxada frontal na polia", 3, 8, 12, 0.0, 90), ex("Cadeira extensora", 3, 12, 15, 0.0, 60),
                ex("Mesa flexora", 3, 10, 15, 0.0, 60), ex("Prancha isométrica", 3, 30, 60, 0.0, 45)
            ))
        )),
        TemplateDef("5dias", "Treino de 5 dias", "PPL + Upper/Lower", listOf(
            DaySpec("PUSH", 1, "07:00", listOf(
                ex("Supino reto com barra", 4, 6, 10, 0.0, 120), ex("Supino máquina", 3, 8, 12, 0.0, 90),
                ex("Desenvolvimento com halteres", 3, 8, 12, 0.0, 90), ex("Elevação lateral", 4, 12, 15, 0.0, 60),
                ex("Tríceps na polia (corda/barra)", 3, 10, 12, 0.0, 60)
            )),
            DaySpec("PULL", 2, "07:00", listOf(
                ex("Barra fixa (pull-up)", 4, 6, 10, 0.0, 120), ex("Remada curvada com barra", 3, 8, 12, 0.0, 120),
                ex("Puxada frontal na polia", 3, 8, 12, 0.0, 90), ex("Crucifixo inverso", 3, 12, 15, 0.0, 60),
                ex("Rosca direta com barra", 3, 8, 12, 0.0, 60)
            )),
            DaySpec("LEGS", 3, "07:00", listOf(
                ex("Agachamento livre", 4, 6, 10, 0.0, 150), ex("Leg press 45°", 3, 8, 12, 0.0, 120),
                ex("Mesa flexora", 3, 10, 12, 0.0, 90), ex("Panturrilha em pé", 4, 12, 15, 0.0, 60)
            )),
            DaySpec("UPPER (FORÇA)", 5, "07:00", listOf(
                ex("Supino inclinado com halteres", 4, 6, 8, 0.0, 150), ex("Remada unilateral com halter", 4, 6, 8, 0.0, 120),
                ex("Desenvolvimento máquina", 3, 6, 8, 0.0, 120), ex("Rosca no banco Scott", 3, 8, 10, 0.0, 60),
                ex("Tríceps testa", 3, 8, 10, 0.0, 60)
            )),
            DaySpec("LOWER + CORE", 6, "08:00", listOf(
                ex("Levantamento terra", 4, 5, 8, 0.0, 180), ex("Afundo (lunge)", 3, 10, 12, 0.0, 90),
                ex("Elevação pélvica (hip thrust)", 3, 8, 12, 0.0, 90), ex("Abdominal infra (canivete)", 3, 12, 15, 0.0, 45),
                ex("Esteira (caminhada/corrida)", 1, 15, 20, 0.0, 0)
            ))
        ))
    )
}
