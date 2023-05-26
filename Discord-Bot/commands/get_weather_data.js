const { SlashCommandBuilder } = require('discord.js');
// const { request } = require('undici')

const url =
  'https://awu4j6hku3.execute-api.eu-central-1.amazonaws.com/dev/weather/latest';

module.exports = {
  data: new SlashCommandBuilder()
    .setName('get-weather-data')
    .setDescription('get weather data from the LTAI Brad weather station'),

  async execute(interaction) {
    const response = await fetch(url);
    const data = await response.json();

    const temperature = 'Temperature: ' + data.temp.value.toString() + 'C\n';
    const humidity = 'Humidity: ' + data.humidity.value.toString() + '%\n';
    const pressure = 'Pressure: ' + data.pressure.value.toString() + 'hPa\n';
    const windSpeed = 'Wind Speed: ' + data.wind.value.toString() + 'km/h\n';

    await interaction.reply(
      '```' + temperature + humidity + pressure + windSpeed + '```'
    );
  },
};
