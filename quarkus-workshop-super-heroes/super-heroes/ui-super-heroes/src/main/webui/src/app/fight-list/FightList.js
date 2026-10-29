export function FightList({fights}) {

  return (
    <table className="table table-striped">
      <thead>
      <tr>
        <th className="fight-list-header thead-dark">Id</th>
        <th className="fight-list-header thead-dark">Fight Date</th>
        <th className="fight-list-header thead-dark">Winner</th>
        <th className="fight-list-header thead-dark">Loser</th>
      </tr>
      </thead>
      <tbody>


      {fights && fights.map(element => (
        <tr key={element.id}>
          <td> {element.id} </td>
          <td> {element.fightDate} </td>
          <td> {element.winnerName} </td>
          <td> {element.loserName} </td>
        </tr>))}
      </tbody>
    </table>
  )
}

