let itemResponse = {
    status: 200,
    body: {
        id: '12345',
        name: 'name',
        description: 'description',
        amount: '1',
    }
}

let itemsResponse = {
    status: 200,
    body: [{
        id: '12345',
        name: 'name',
        description: 'description',
        amount: '1',
    }]
}

const mockFetch = async (url, data) => {
    switch(data.method) {
        case 'GET':
            if (url.includes('/item/')) {
                return {
                    status: itemResponse.status,
                    json: () => itemResponse.body,
                }
            } else if (url.endsWith('/item')) {
                return {
                    status: itemsResponse.status,
                    json: () => itemsResponse.body,
                }
            }
            break

        case 'POST':
            if (url.includes('item')) {
                return {
                    status: itemResponse.status,
                    json: async () => itemResponse.body,
                };
            }
            break

        case 'PUT':
            if (url.includes('item')) {
                return {
                    ok: true,
                    status: itemResponse.status,
                    json: async () => itemResponse.body,
                }
            }
            break

        case 'DELETE':
            if (url.includes('/item/')) {
                return {
                    status: 200,
                };
            }
            break

        default:
            throw new Error(`Undefined fetch mock for url: ${url} and method: ${data.method}`);
    }
}

export {
    mockFetch,
    itemResponse,
    itemsResponse
}