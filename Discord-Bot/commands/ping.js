const { SlashCommandBuilder } = require('discord.js');

module.exports = {
  data: new SlashCommandBuilder().setName('ping').setDescription('test bot'),

  async execute(interaction) {
    await interaction.reply('fuck you');
  },
};
