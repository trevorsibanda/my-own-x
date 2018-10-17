window.onload = function(){
    var pastes_app = new Vue( {
          el: '#recent_pastes',
          data: {
          exchange: {
              name: 'Golix '
          },
          coins: [
              {
              name: 'Bitcoin',
              symbol: 'BTC'
              },
              {
              name: 'Ethereum',
              symbol: 'ETH'
              }
          ]
          }
      });
     
    }