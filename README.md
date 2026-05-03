# 📱 Micro Rede Social

> Projeto Android desenvolvido como parte do curso de Desenvolvimento Mobile no IFSP - Campus Araraquara (Análise e Desenvolvimento de Sistemas).

## Descrição
O **Micro Rede Social** é um aplicativo mobile focado no compartilhamento de momentos baseado em localização. Os usuários podem criar perfis, postar fotos de suas viagens ou do dia a dia, e explorar um feed global de postagens filtrável por cidade. O projeto utiliza infraestrutura em nuvem (BaaS) para garantir sincronização de dados em tempo real.

### 🚀 Funcionalidades
- **Autenticação de Usuários:** Cadastro, login e validação de sessão utilizando Firebase Auth.
- **Gestão de Perfil:** Atualização de nome e foto de perfil.
- **Publicação de Posts:** Inserção de foto via Galeria do dispositivo (API `PickVisualMediaRequest`) e descrição.
- **Geolocalização Integrada:** Captura automática da cidade atual do usuário usando `FusedLocationProviderClient` e `Geocoder` (GPS).
- **Feed Paginado:** Leitura eficiente de dados do banco utilizando Paginação por Cursor ("Seek"), carregando dados em blocos para economizar banda e memória.
- **Busca Inteligente:** Filtro de postagens por cidade que ignora acentuação e letras maiúsculas/minúsculas (técnica de desnormalização de dados).

## 🎥 Demonstração

<!-- DICA: Grave um vídeo no Android Studio conforme as instruções do Professor, suba no YouTube ou arraste o arquivo .mp4 direto aqui pro GitHub e substitua o link abaixo! -->
[![Assista à demonstração](aindanãotemvídeo:P)

*Acima: Vídeo demonstrando o fluxo de login, criação de postagem com GPS e busca no feed.*

## 🛠️ Tecnologias Utilizadas
- **Linguagem:** Kotlin
- **IDE:** Android Studio
- **Interface:** XML com ConstraintLayout, CardView e Material Components
- **Arquitetura & Ferramentas:** ViewBinding, RecyclerView + Adapter personalizado
- **Backend as a Service (BaaS):**
  - Firebase Authentication (Gestão de Identidade)
  - Firebase Cloud Firestore (Banco de Dados NoSQL em Nuvem)
- **Serviços de Localização:** Google Play Services Location (Fused Location & Geocoder)
- **Tratamento de Imagem:** Compressão de Bitmaps e conversão para formato `Base64`.

## 📌 Status
✔️ **Concluído** - Aplicativo funcional com todas as operações de CRUD, integração com GPS e paginação de banco de dados rodando em nuvem.

## 🧠 Aprendizados e Desafios
Durante o desenvolvimento deste projeto, enfrentei e superei desafios técnicos de nível intermediário/avançado:
- **Paginação em NoSQL:** Implementação de paginação por cursor (`startAfter`) no Firestore, criando um histórico de cursores na memória para permitir a navegação bidirecional (Avançar/Voltar), superando a ausência do tradicional `OFFSET`.
- **Limites de Nuvem:** Tratamento de imagens (compressão e redimensionamento em tempo de execução) para conversão segura em String Base64, respeitando o limite de 1MB por documento do Firestore.
- **Filtros e Índices Compostos:** Adaptação de buscas no Firestore para ignorar *case* e diacríticos, além da configuração de **Composite Indexes** diretamente no console do Firebase para permitir consultas complexas ordenadas por data.
- **Geocodificação:** Extração e tratamento inteligente de dados de latitude/longitude para obter o nome legível da cidade usando a classe `Geocoder`.

## ⚙️ Instalação e Uso

Para clonar e testar o projeto localmente:

```bash
# Clone o repositório
git clone https://github.com/SEU_USUARIO/MicroRedeSocialGisotra.git

# Entre na pasta
cd MicroRedeSocialGisotra

# Abra o projeto no Android Studio e aguarde o Gradle Sync.
