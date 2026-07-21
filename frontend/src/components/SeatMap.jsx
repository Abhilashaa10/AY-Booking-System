const SECTION_COLORS = {
  VIP: 'bg-purple-100 border-purple-300 text-purple-700 hover:bg-purple-200',
  PREMIUM: 'bg-blue-100 border-blue-300 text-blue-700 hover:bg-blue-200',
  GENERAL: 'bg-green-100 border-green-300 text-green-700 hover:bg-green-200',
}

export default function SeatMap({ seats, selectedSeatId, onSelect }) {
  const rows = {}
  seats.forEach((seat) => {
    if (!rows[seat.rowLabel]) rows[seat.rowLabel] = []
    rows[seat.rowLabel].push(seat)
  })

  const sortedRowLabels = Object.keys(rows).sort()

  return (
    <div className="space-y-2">
      {sortedRowLabels.map((rowLabel) => (
        <div key={rowLabel} className="flex items-center gap-2">
          <span className="w-6 text-sm font-medium text-gray-400">{rowLabel}</span>
          <div className="flex gap-1.5 flex-wrap">
            {rows[rowLabel]
              .sort((a, b) => a.seatNumber.localeCompare(b.seatNumber, undefined, { numeric: true }))
              .map((seat) => {
                const isBooked = seat.status !== 'AVAILABLE'
                const isSelected = seat.id === selectedSeatId
                return (
                  <button
                    key={seat.id}
                    disabled={isBooked}
                    onClick={() => onSelect(seat)}
                    title={`${seat.seatNumber} · ₹${seat.price} · ${seat.status}`}
                    className={`w-9 h-9 rounded-md border text-xs font-medium flex items-center justify-center transition-all ${isBooked ? 'bg-gray-100 border-gray-200 text-gray-300 cursor-not-allowed' : SECTION_COLORS[seat.section] || 'bg-gray-50 border-gray-200'} ${isSelected ? 'ring-2 ring-indigo-500 scale-110' : ''}`}
                  >
                    {seat.seatNumber}
                  </button>
                )
              })}
          </div>
        </div>
      ))}
    </div>
  )
}
