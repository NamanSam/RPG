// Original vector pixel art. Integer geometry stays crisp at every screen size.
function Tree({ x, y, pine = false, color = "#39794c" }) {
  return (
    <g transform={`translate(${x} ${y})`}>
      <path fill="#674a36" d="M-3 0h6v17h-6z" />
      {pine ? (
        <>
          <path fill="#214d42" d="M-17 3h34v-8h-5v-9h-6v-10H-6v10h-6v9h-5z" />
          <path fill={color} d="M-12-6h24v-7H7v-10H-3v8h-5v5h-4z" />
        </>
      ) : (
        <>
          <path fill="#285439" d="M-18-4h36v-19h-6v-7H-9v5h-9z" />
          <path fill={color} d="M-17-9h26v-18H-8v5h-9z" />
          <path fill="#79a656" d="M-12-20h9v5h-9z" />
        </>
      )}
    </g>
  );
}
function House({ x, y, roof = "#b25d49" }) {
  return (
    <g transform={`translate(${x} ${y})`}>
      <path fill="#5d6045" opacity=".25" d="M-33 23h77v10h-77z" />
      <path fill="#e7c991" d="M-27-6h54v34h-54z" />
      <path fill="#967345" d="M-27 23h54v5h-54zM-29-8h58v5h-58z" />
      <path fill={roof} d="M-36-8h72v-8H28v-8H20v-8h-40v8h-8v8h-8z" />
      <path fill="#edb178" d="M-22-27h39v4h-39zM-29-17h56v3h-56z" />
      <path fill="#785038" d="M-6 8H8v20H-6zM-20 1h9v11h-9zM15 1h9v11h-9z" />
      <path fill="#eed887" d="M-18 3h5v6h-5zM17 3h5v6h-5z" />
      <path fill="#745547" d="M19-37h7v15h-7z" />
    </g>
  );
}
function Palm({ x, y }) {
  return (
    <g transform={`translate(${x} ${y})`}>
      <path fill="#af7943" d="M0 0h7v-19H3v-17h-6v-17h-6v6h4v16h4z" />
      <path
        fill="#32674c"
        d="M-8-47h-21v6h-10v10h7v-6h21v-5H2v-6h23v8h8v12h6v-17h-8v-8H8v-8H-9v-6h-20v7h14z"
      />
      <path fill="#548849" d="M-7-49h-17v6h15v5h7v15h6v-24h15v-6H1v-6h-13z" />
      <path fill="#79583b" d="M-5-43h6v6h-6z" />
    </g>
  );
}
export function Npc({ color = "#d76d46" }) {
  return (
    <svg
      className="npc-sprite"
      viewBox="0 0 32 44"
      aria-hidden="true"
      shapeRendering="crispEdges"
    >
      <ellipse cx="16" cy="40" rx="12" ry="3" fill="#183932" opacity=".3" />
      <g className="npc-body">
        <path fill="#45372f" d="M10 30h5v9H8v-4h2zm8 0h5v5h2v4h-7z" />
        <path fill={color} d="M8 17h17v16H8zM4 20h4v9H4zM25 20h4v9h-4z" />
        <path fill="#f4c594" d="M9 6h15v13H9zM4 29h4v4H4zM25 29h4v4h-4z" />
        <path fill="#4a3834" d="M8 4h17v6H8zM11 12h2v3h-2zM20 12h2v3h-2z" />
        <path fill="#f6d27f" d="M6 5h21v4H6zM11 0h12v5H11zM8 24h17v3H8z" />
        <path fill="#fff0bf" d="M14 18h5v4h-5z" />
      </g>
    </svg>
  );
}
export function PixelScene({ theme }) {
  const trees = Array.from({ length: 36 }, (_, i) => ({
    x: (i * 83 + 17) % 800,
    y: 130 + ((i * 67) % 330),
  }));
  const green = theme === "village" || theme === "forest";
  const base = {
    beach: "#e9cc8c",
    village: "#8eac65",
    forest: "#527c58",
    canyon: "#c78a65",
    kingdom: "#8b9b8b",
  }[theme];
  return (
    <svg
      className={`pixel-scene ${theme}`}
      viewBox="0 0 800 480"
      preserveAspectRatio="xMidYMid slice"
      shapeRendering="crispEdges"
      aria-hidden="true"
    >
      <rect width="800" height="480" fill={base} />
      {Array.from({ length: 140 }, (_, i) => (
        <path
          key={i}
          fill={green ? "#b4c87b" : "#f7e2ad"}
          opacity=".22"
          d={`M${(i * 137) % 800} ${(i * 71) % 480}h${i % 3 ? 4 : 9}v3h-4v3h-5z`}
        />
      ))}
      {theme === "beach" && (
        <>
          <path
            fill="#8bc3b0"
            d="M0 0h800v58H720v14H590v-9H420v22H270v-8H160v19H80v-8H0zM0 160h44v28h19v75h26v70h-9v59h40v88H0z"
          />
          <path
            fill="#4ba4a5"
            d="M0 0h800v39H710v16H590v-9H420v21H270v-8H150v20H70V65H0zM0 183h23v24h22v71h25v61H59v72h40v69H0z"
          />
          <g className="water-ripples" fill="#b9e4c7" opacity=".65">
            {[30, 150, 310, 460, 620, 740].map((x, i) => (
              <path key={x} d={`M${x} ${20 + (i % 2) * 18}h35v3h-35z`} />
            ))}
            <path d="M8 270h20v3H8zM25 369h24v3H25zM19 439h30v3H19z" />
          </g>
          <path
            fill="#faf0c3"
            d="M90 122h109v5H90zM580 92h100v5H580zM107 354h35v4h-35z"
          />
          <g transform="translate(653 190)">
            <path fill="#ac9562" d="M-26 58h71v9h-71z" />
            <path fill="#f4e6b4" d="M-13-41h32V60h-32z" />
            <path
              fill="#c76b4d"
              d="M-13-19h32v19h-32zM-13 26h32v18h-32zM-22-45h49v-7H18v-10H-12v10h-10z"
            />
            <path fill="#526e66" d="M-7-39h21v17H-7z" />
            <path fill="#fae391" d="M-3-37h13v13H-3z" />
            <path fill="#655447" d="M-3 46h13v14H-3z" />
          </g>
          <Palm x={164} y={215} />
          <Palm x={203} y={245} />
          <Palm x={725} y={381} />
          <Palm x={110} y={420} />
          <Palm x={578} y={392} />
          <g transform="translate(272 328)">
            <path fill="#75583e" d="M0 0h53v8H0zM4 8h4v25H4zM44 8h4v25h-4z" />
            <path fill="#a47b4a" d="M0-7h53v6H0zM5-4h2v5H5z" />
            <path fill="#b5573f" d="M10-24h28v18H10z" />
            <path fill="#f3ce7c" d="M10-22h28v4H10zM22-22h4v16h-4z" />
          </g>
          <path
            fill="#cf9b6a"
            d="M463 176h9v5h-9zM470 171h4v15h-4zM519 363h8v5h-8z"
          />
        </>
      )}
      <path
        className="map-trail"
        fill="none"
        stroke={theme === "canyon" ? "#e7b984" : "#e0c795"}
        strokeWidth="27"
        d="M400-15v104h-75v80h110v111h-65v111h30v100"
      />
      <path
        fill="none"
        stroke="#f4dfaf"
        strokeWidth="15"
        opacity=".5"
        strokeDasharray="5 8"
        d="M400-15v104h-75v80h110v111h-65v111h30v100"
      />
      {theme === "beach" && <path fill="#e9cc8c" d="M380 0h40v83h-40z" />}
      {theme === "village" && (
        <>
          <path
            fill="#699f9b"
            d="M610 0h38v100h-23v81h26v119h-30v180h-37V278h28v-78h-28V88h26z"
          />
          <path
            fill="#a4c8ad"
            opacity=".6"
            d="M621 21h4v54h-4zM627 233h4v41h-4zM596 354h4v67h-4z"
          />
          <House x={197} y={192} />
          <House x={538} y={155} roof="#627f83" />
          <House x={577} y={358} />
          <House x={172} y={379} />
          <path fill="#795e41" d="M580 246h80v8h-80zM580 265h80v8h-80z" />
          <path fill="#c19e66" d="M580 254h80v11h-80z" />
          {trees
            .filter((t) => t.x < 100 || t.x > 710)
            .map((t, i) => (
              <Tree key={i} {...t} />
            ))}
          <g fill="#f6dda0">
            {[200, 234, 260].map((x) => (
              <path key={x} d={`M${x} 286h4v12h-4zM${x - 3} 287h10v3h-10z`} />
            ))}
          </g>
        </>
      )}
      {theme === "forest" && (
        <>
          {trees
            .filter((t) => t.x < 280 || t.x > 535)
            .map((t, i) => (
              <Tree key={i} {...t} pine color={i % 2 ? "#42754a" : "#58834d"} />
            ))}
          <path
            fill="#75a59a"
            d="M593 326h91v11h25v32h-19v14h-95v-10h-19v-31h17z"
          />
          <path fill="#b1c0a0" d="M609 344h38v3h-38zM658 363h23v3h-23z" />
          <g fill="#deb793">
            {[210, 545, 300, 650].map((x, i) => (
              <g key={x}>
                <path fill="#d78b73" d={`M${x} ${220 + i * 37}h12v5h-12z`} />
                <path d={`M${x + 4} ${225 + i * 37}h4v6h-4z`} />
              </g>
            ))}
          </g>
          <g className="fireflies" fill="#f1e898">
            <path d="M266 190h3v3h-3zM520 250h3v3h-3zM319 337h3v3h-3zM566 396h3v3h-3z" />
          </g>
        </>
      )}
      {theme === "canyon" && (
        <>
          {[20, 120, 610, 730].map((x, i) => (
            <g key={x} transform={`translate(${x} ${140 + (i % 2) * 120})`}>
              <path fill="#945e53" d="M-37 130V15h13V-5h55v20h12v115z" />
              <path fill="#dfac7b" d="M-24-5h55v18h-55z" />
              <path fill="#ad7056" d="M-37 28h80v13h-80zM-37 71h80v9h-80z" />
            </g>
          ))}
          <path fill="#75514a" d="M284 330h208v64H284z" />
          <path fill="#c99f70" d="M325 330h90v64h-90z" />
          {Array.from({ length: 8 }, (_, i) => (
            <path key={i} fill="#7d5e46" d={`M325 ${330 + i * 8}h90v2h-90z`} />
          ))}
          <path
            fill="#587459"
            d="M202 410h8v-37h-8zM193 380h6v15h11v-6h-11v-9zM210 391h13v-19h-6v13h-7z"
          />
        </>
      )}
      {theme === "kingdom" && (
        <>
          <path fill="#717c78" d="M97 0h80v480H97zM644 0h66v480h-66z" />
          <path fill="#a7bca4" d="M109 0h5v480h-5zM657 0h4v480h-4z" />
          <g transform="translate(505 137)">
            <path fill="#586779" d="M0 0h155v142H0z" />
            <path fill="#b4bdb0" d="M6 8h144v130H6z" />
            <path fill="#d1d0b5" d="M-14-19h34v164h-34zM132-19h34v164h-34z" />
            <path
              fill="#647b89"
              d="M-22-19h50v-10h-8v-12H-3v12h-19zM124-19h50v-10h-8v-12h-23v12h-19z"
            />
            <path
              fill="#516371"
              d="M57 88h39v50H57zM40 23h13v22H40zM99 23h13v22H99z"
            />
            <path fill="#dbb46e" d="M61 92h31v46H61z" />
            <path
              fill="#af6470"
              d="M28 52h17v32l-8-5-9 5zM107 52h17v32l-8-5-9 5z"
            />
          </g>
          <Tree x={220} y={219} color="#799277" />
          <Tree x={207} y={368} color="#799277" />
          <Tree x={580} y={396} color="#799277" />
          <path fill="#697e83" d="M283 180h45v8h-45zM289 169h33v11h-33z" />
          <path fill="#bad4bb" d="M300 151h10v18h-10z" />
        </>
      )}
      <g fill="#6a7052" opacity=".4">
        {[0, 1, 2, 3, 4, 5].map((n) => (
          <path key={n} d={`M${470 + n * 7} ${425 + (n % 2) * 6}h3v3h-3z`} />
        ))}
      </g>
    </svg>
  );
}
