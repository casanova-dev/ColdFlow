const seed = {
  batches: [
    { batchId: 101, productId: 'P01', productName: 'MilkPack', quantity: 50, expiryDate: '2026-10-15', zone: 'Zone-A' },
    { batchId: 102, productId: 'P02', productName: 'Cheese', quantity: 30, expiryDate: '2026-11-02', zone: 'Zone-B' }
  ],
  inventory: [
    { productId: 'P01', batchId: 101, quantity: 50, zone: 'Zone-A' },
    { productId: 'P02', batchId: 102, quantity: 30, zone: 'Zone-B' }
  ],
  zones: [
    { zoneId: 1, zoneName: 'ColdRoomA', min: 2, max: 8, current: 6.5 },
    { zoneId: 2, zoneName: 'ColdRoomB', min: 1.5, max: 7, current: 9.8 }
  ]
};

let data = JSON.parse(localStorage.getItem('coldflow-data') || 'null') || structuredClone(seed);
const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => document.querySelectorAll(selector);

function saveData() {
  localStorage.setItem('coldflow-data', JSON.stringify(data));
  renderAll();
}

function toast(message) {
  const el = $('#toast');
  el.textContent = message;
  el.classList.add('show');
  setTimeout(() => el.classList.remove('show'), 2200);
}

function formData(form) {
  return Object.fromEntries(new FormData(form).entries());
}

function setMessage(id, text, error = false) {
  const el = $(id);
  el.textContent = text;
  el.classList.toggle('error', error);
}

function renderBatches() {
  const search = ($('#batch-search')?.value || '').toLowerCase();
  const rows = data.batches
    .filter((item) => Object.values(item).some((value) => String(value).toLowerCase().includes(search)))
    .map((item) => `
      <tr>
        <td>#${item.batchId}</td>
        <td>${item.productName}<br><small>${item.productId}</small></td>
        <td>${item.quantity}</td>
        <td>${item.expiryDate}</td>
        <td><span class="zone-status normal">${item.zone}</span></td>
        <td><button class="delete-btn" data-delete-batch="${item.batchId}" type="button">Delete</button></td>
      </tr>
    `).join('');

  $('#batch-table').innerHTML = rows || '<tr><td colspan="6">No matching batches found.</td></tr>';
}

function renderInventory(direction = 'forward') {
  const items = direction === 'backward' ? [...data.inventory].reverse() : data.inventory;
  $('#inventory-list').innerHTML = items.map((item) => `
    <div class="inventory-item">
      <div>
        <strong>${item.productId} · Batch #${item.batchId}</strong>
        <small>${item.zone} · ${direction} traversal</small>
      </div>
      <input class="quantity-edit" type="number" min="0" value="${item.quantity}" data-update-inventory="${item.batchId}" />
    </div>
  `).join('') || '<div class="inventory-item"><div><strong>No inventory items.</strong></div></div>';
}

function renderZones() {
  $('#zone-list').innerHTML = data.zones.map((zone) => {
    const healthy = zone.current >= zone.min && zone.current <= zone.max;
    return `
      <div class="zone-item">
        <div>
          <strong>Zone ${zone.zoneId} · ${zone.zoneName}</strong>
          <small>${zone.current.toFixed(1)}°C · range ${zone.min}°C to ${zone.max}°C</small>
        </div>
        <span class="zone-status ${healthy ? 'normal' : 'alert'}">${healthy ? 'NORMAL' : 'ALERT'}</span>
      </div>
    `;
  }).join('') || '<div class="zone-item"><div><strong>No zones registered.</strong></div></div>';
}

function renderOverviewStats() {
  const totalUnits = data.inventory.reduce((sum, item) => sum + Number(item.quantity), 0);
  const healthyZones = data.zones.filter((zone) => zone.current >= zone.min && zone.current <= zone.max).length;
  const avgTemp = data.zones.length ? (data.zones.reduce((sum, z) => sum + z.current, 0) / data.zones.length).toFixed(1) : '0.0';
  const alertCount = data.zones.filter((zone) => !(zone.current >= zone.min && zone.current <= zone.max)).length;

  $('#stat-batches').textContent = data.batches.length;
  $('#stat-units').textContent = totalUnits;
  $('#stat-normal').textContent = healthyZones;
  $('#stat-normal-label').textContent = `/ ${data.zones.length} total`;
  $('#dispatch-q').textContent = data.batches.length;
  $('#alert-count').textContent = alertCount;
  $('#avg-temp').textContent = `${avgTemp}°C`;
}

function renderAll() {
  renderBatches();
  renderInventory();
  renderZones();
  renderOverviewStats();
}

function showView(name) {
  $$('.view').forEach((view) => view.classList.toggle('active', view.id === `${name}-view`));
  $$('.nav-link').forEach((link) => link.classList.toggle('active', link.dataset.view === name));
  const titles = {
    overview: 'Management system overview',
    cg: 'CG implementation',
    java: 'Java implementation',
    batches: 'Batch management',
    inventory: 'Inventory flow',
    zones: 'Zone monitoring',
    demo: 'CO2 demonstration'
  };
  $('#page-title').textContent = titles[name] || titles.overview;
  history.replaceState(null, '', `#${name}`);
}

function runDemo() {
  $('#demo-console').textContent = `$ coldflow --co2-demo\n\n[INPUT] Samyak / Batch records\n  Batch #101 · MilkPack · quantity 50 · Zone-A\n[OUTPUT] Batch inserted successfully.\n  Ledger count: 2\n\n[INPUT] Sarthak / Inventory flow\n  Product P01 · Batch #101 · quantity 50\n[OUTPUT] Forward traversal > 101, 102\n         Backward traversal > 102, 101\n\n[INPUT] Zaki / Zone monitor\n  ColdRoomA range 2°C–8°C · current 6.5°C\n[OUTPUT] Status: NORMAL\n\n[INPUT] Java / CG integration\n  Dashboard + InventoryManager + StorageZone\n[OUTPUT] Live monitoring active\n\n✓ CO2 demonstration complete.`;
  toast('CO2 demonstration completed');
}

function cycleCG() {
  const freezer = {
    r0: -20.0,
    r1: (Math.random() * 3.0 - 1.8).toFixed(1),
    r2: (Math.random() * 2.0 + 1.2).toFixed(1),
    r3: 'ON',
    r4: (12 + Math.random() * 2.8).toFixed(1)
  };

  const chiller = {
    r0: 4.0,
    r1: (Math.random() * 2.8 + 4.7).toFixed(1),
    r2: (Math.random() * 1.8 - 1.9).toFixed(1),
    r3: 'IDLE',
    r4: (8 + Math.random() * 3.1).toFixed(1)
  };

  const ambient = {
    r0: 20.0,
    r1: (Math.random() * 4.0 + 20.7).toFixed(1),
    r2: (Math.random() * 3.2 - 2.4).toFixed(1),
    r3: 'OFF',
    r4: (3.8 + Math.random() * 2.4).toFixed(1)
  };

  $('#cg-f-r0').textContent = freezer.r0.toFixed(1);
  $('#cg-f-r1').textContent = freezer.r1;
  $('#cg-f-r2').textContent = freezer.r2;
  $('#cg-f-r3').textContent = freezer.r3;
  $('#cg-f-r4').textContent = `${freezer.r4}kWh`;

  $('#cg-c-r0').textContent = chiller.r0.toFixed(1);
  $('#cg-c-r1').textContent = chiller.r1;
  $('#cg-c-r2').textContent = chiller.r2;
  $('#cg-c-r3').textContent = chiller.r3;
  $('#cg-c-r4').textContent = `${chiller.r4}kWh`;

  $('#cg-a-r0').textContent = ambient.r0.toFixed(1);
  $('#cg-a-r1').textContent = ambient.r1;
  $('#cg-a-r2').textContent = ambient.r2;
  $('#cg-a-r3').textContent = ambient.r3;
  $('#cg-a-r4').textContent = `${ambient.r4}kWh`;
}

$$('.nav-link').forEach((link) => {
  link.addEventListener('click', (event) => {
    event.preventDefault();
    showView(link.dataset.view);
  });
});

$$('[data-go]').forEach((button) => {
  button.addEventListener('click', () => showView(button.dataset.go));
});

$('#batch-form').addEventListener('submit', (event) => {
  event.preventDefault();
  const values = formData(event.target);

  if (data.batches.some((batch) => batch.batchId === Number(values.batchId))) {
    setMessage('#batch-message', 'Duplicate Batch ID. Please use a unique ID.', true);
    return;
  }

  data.batches.push({
    batchId: Number(values.batchId),
    productId: values.productId,
    productName: values.productName,
    quantity: Number(values.quantity),
    expiryDate: values.expiryDate,
    zone: values.zone
  });

  setMessage('#batch-message', 'Batch inserted successfully.');
  event.target.reset();
  saveData();
  toast('Batch +1');
});

$('#batch-search').addEventListener('input', renderBatches);
$('#clear-batch-search').addEventListener('click', () => {
  $('#batch-search').value = '';
  renderBatches();
});

$('#batch-table').addEventListener('click', (event) => {
  const id = event.target.dataset.deleteBatch;
  if (!id) return;
  data.batches = data.batches.filter((batch) => batch.batchId !== Number(id));
  saveData();
  toast(`Batch ${id} deleted`);
});

$('#inventory-form').addEventListener('submit', (event) => {
  event.preventDefault();
  const values = formData(event.target);

  if (data.inventory.some((item) => item.batchId === Number(values.batchId))) {
    setMessage('#inventory-message', 'Duplicate batch ID. Inventory item already exists.', true);
    return;
  }

  data.inventory.push({
    productId: values.productId,
    batchId: Number(values.batchId),
    quantity: Number(values.quantity),
    zone: values.zone
  });

  setMessage('#inventory-message', 'Inventory item inserted successfully.');
  event.target.reset();
  saveData();
  toast('Inventory updated');
});

$$('.direction').forEach((button) => {
  button.addEventListener('click', () => {
    $$('.direction').forEach((item) => item.classList.toggle('active', item === button));
    renderInventory(button.dataset.direction);
  });
});

$('#inventory-list').addEventListener('change', (event) => {
  const id = event.target.dataset.updateInventory;
  if (!id) return;
  const item = data.inventory.find((record) => record.batchId === Number(id));
  if (!item) return;
  item.quantity = Math.max(0, Number(event.target.value));
  saveData();
  toast('Quantity updated successfully');
});

$('#zone-form').addEventListener('submit', (event) => {
  event.preventDefault();
  const values = formData(event.target);

  if (Number(values.max) < Number(values.min)) {
    setMessage('#zone-message', 'Maximum temperature must be greater than minimum.', true);
    return;
  }

  if (data.zones.some((zone) => zone.zoneId === Number(values.zoneId))) {
    setMessage('#zone-message', 'Duplicate zone ID. Please use a unique ID.', true);
    return;
  }

  data.zones.push({
    zoneId: Number(values.zoneId),
    zoneName: values.zoneName,
    min: Number(values.min),
    max: Number(values.max),
    current: Number(values.current)
  });

  setMessage('#zone-message', 'Zone inserted successfully.');
  event.target.reset();
  saveData();
  toast('Zone added');
});

$('#monitor-zones').addEventListener('click', () => {
  renderZones();
  toast('Circular zone traversal complete');
});

$('#run-demo').addEventListener('click', runDemo);
$('#cg-run').addEventListener('click', () => {
  cycleCG();
  toast('CG cycle executed');
});

$('#reset-data').addEventListener('click', () => {
  data = structuredClone(seed);
  localStorage.setItem('coldflow-data', JSON.stringify(data));
  renderAll();
  toast('Demo data reset');
});

window.addEventListener('hashchange', () => {
  const name = location.hash.replace('#', '') || 'overview';
  showView(name);
});

renderAll();
showView(location.hash.replace('#', '') || 'overview');
