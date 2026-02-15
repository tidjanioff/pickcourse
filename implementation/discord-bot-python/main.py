import discord
# permettant de créer des commandes slash
from discord import app_commands
# pour créer un formulaire interactif
from discord.ui import Modal, TextInput
# pour get les variables environnementales
import os
# biblio qui contient les insultes
from better_profanity import profanity
import re
from dotenv import load_dotenv
import requests
# url de notre api
API_URL = "http://localhost:7070/avis"

load_dotenv()  # charge les variables du fichier .env

#profanity.load_censor_words(profanity.CENSOR_WORDS + mots_interdits_fr)

# Ce bloc de code permet de créer un client Discord ( représentation du bot dans le code)
intents = discord.Intents.default()
intents.members = True  
# les intents précisent quelles infos le bot va recevoir
client = discord.Client(intents=intents)
# on crée un arbre de commandes lié au client pour pouvoir utiliser les slash commands
tree = app_commands.CommandTree(client)

# Cette classe permet de créer le formulaire d'Avis.
class CoursModal(Modal):
    # initialisation du formulaire
    def __init__(self):
        super().__init__(title="Formulaire Avis")
        # Champ texte pour le nom du cours
        #!!!! Et si le user ne connaît pas le sigle?
        self.nom_cours = TextInput(label="Sigle du cours", placeholder="Ex : IFT2255", required=True)
        self.professeur = TextInput(label="Nom du professeur", placeholder="Entrez le nom du prof")
        self.note_difficulte = TextInput(label="Note difficulté", placeholder="Nombre entier entre 0 et 5", required=True)
        #self.note_qualite = TextInput(label="Note qualité", placeholder="Nombre entier entre 0 et 5", required=True)
        self.note_charge = TextInput(label="Charge de travail", placeholder="Nombre entier entre 0 et 5", required=True)
        # !!! required?
        self.commentaire = TextInput(label="Commentaire", placeholder="Entrez votre commentaire ici")

        self.add_item(self.nom_cours)
        self.add_item(self.commentaire)
        self.add_item(self.professeur)
        self.add_item(self.note_difficulte)
        #self.add_item(self.note_qualite)
        self.add_item(self.note_charge)

    async def on_submit(self, interaction: discord.Interaction):
        # pattern pour le sigle du cours ( cela permet de faire une validation minimale de l'input)
        # on vérifiera si ça correspond bien à un sigle de cours de l'Udem dans AvisService.
        if not re.match(r"^[A-Z]{3}\d{4}$", self.nom_cours.value.upper()):
            await interaction.response.send_message(
                "Erreur : le sigle du cours doit être au format 3 lettres suivies de 4 chiffres (ex: IFT2255).",
                ephemeral=True
            )
            return
        
        # Vérification des notes (difficulté, qualité, charge)
        for champ, nom in [(self.note_difficulte, "difficulté"), 
                           (self.note_charge, "charge de travail")]:
            try:
                valeur = float(champ.value)
                if not (0 <= valeur <= 5):
                    raise ValueError()
            except ValueError:
                await interaction.response.send_message(
                    f"Erreur : la note de {nom} doit être un nombre entre 0 et 5.",
                    ephemeral=True
                )
                return

        if profanity.contains_profanity(self.commentaire.value):
            await interaction.response.send_message(
                "Erreur : commentaire contient des propos inappropriés.", ephemeral=True
            )
            return
        # Données à envoyer à l'API
        payload = {
            "sigleCours": self.nom_cours.value.upper(),
            "professeur": self.professeur.value,
            "noteDifficulte": int(float(self.note_difficulte.value)),
            "noteCharge": int(float(self.note_charge.value)),
            "commentaire": self.commentaire.value
        }

        try:
            response = requests.post(API_URL, json=payload, timeout=5)

            if response.status_code != 200:
                await interaction.response.send_message(
                    f"Erreur côté serveur : {response.text}",
                    ephemeral=True
                )
                return

        except requests.exceptions.RequestException:
            await interaction.response.send_message(
                "Erreur : impossible de contacter le serveur.",
                ephemeral=True
            )
            return

        #  Tout est correct, on envoie la confirmation
        avis_channel = discord.utils.get(interaction.guild.text_channels, name="avis")
        if avis_channel:
            await avis_channel.send(
                f"Nouvel avis ajouté par {interaction.user.mention} :\n"
                f"**Cours** : {self.nom_cours.value}\n"
                f"**Professeur** : {self.professeur.value}\n"
                f"**Difficulté** : {self.note_difficulte.value}\n"
                f"**Charge de travail** : {self.note_charge.value}\n"
                f"**Commentaire** : {self.commentaire.value}"
            )
        
        #  Ajout pour éviter l'erreur après envoi dans le salon
        await interaction.response.send_message(
            "Votre avis a été ajouté avec succès ! ✅", ephemeral=True
        )

# la commande à saisir
@tree.command(name="avis", description="Cliquez sur la touche Entrée pour remplir le formulaire.")
async def formulaire_cours(interaction: discord.Interaction):
    await interaction.response.send_modal(CoursModal())

@client.event
async def on_member_join(member):
    channel = discord.utils.get(member.guild.text_channels, name="bienvenue")
    if channel:
        await channel.send(
            f"Hello {member.mention}, je suis Danielle 😎!\n"
            "Bienvenue dans le serveur *Avis PickCourse*! \n"
            "Ici, vous pouvez partager vos avis sur les cours de l'UdeM. "
            "Pour poster un avis, allez dans le salon avis, tapez `/avis`, puis cliquez sur la touche Entrée de votre clavier ( ou Send sur mobile), et remplissez le formulaire.\n\n"
            "**Règles à respecter :**\n"
            "- Pas d'insultes ou de mots déplacés\n"
            "- Le sigle du cours doit correspondre à un cours réel de l'UdeM\n"
            "- Les notes doivent être comprises entre 0 et 5\n\n"
            "Sinon, je serai très mécontente, haha 😄!"
        )

@client.event
async def on_ready():
    await tree.sync()  # synchronise les commandes avec Discord
    print(f"Logged in as {client.user}")

client.run(os.getenv("TOKEN"))
